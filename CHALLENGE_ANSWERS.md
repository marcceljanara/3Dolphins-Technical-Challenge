# Challenge Questions - Answers

Dokumen ini berisi jawaban atas pertanyaan arsitektur yang diajukan dalam evaluasi teknis Omnichannel Broadcast Engine.

---

## 1. Architecture: Threading/Asynchronicity Model

**Mengapa memilih model threading ini?**

Pada prototipe ini, kami menggunakan **`ExecutorService` (Single Thread Executor)** yang dipadukan dengan **JSF `@ViewScoped` Managed Bean** dan **Thread-Safe State (`AtomicInteger`, `volatile` flag)**. 

### Alasan Pemilihan & Kelebihan:
1. **Container Independence (Ringan):**
   Model ini tidak bergantung pada Enterprise Java Beans (EJB) container penuh (seperti anotasi `@Asynchronous`). Prototipe dapat berjalan dengan sempurna di embedded servlet container ringan seperti Tomcat 7/8/9 tanpa overhead tambahan.
2. **Kontrol Lifecycle Thread yang Kuat:**
   Dengan memanfaatkan lifecycle callback `@PreDestroy` di managed bean, executor service dapat di-shutdown secara bersih (`executorService.shutdownNow()`) ketika user meninggalkan halaman (view dihancurkan). Hal ini mencegah **Thread Leak** dan penumpukan background task yatim-piatu (*orphan tasks*).
3. **Thread Safety yang Sederhana:**
   Penggunaan variabel asinkron bertipe `AtomicInteger` (`progress`, `successCount`, `failedCount`) dan boolean `volatile` (`broadcastRunning`) memastikan bahwa UI Thread (yang dipicu oleh AJAX polling) dapat membaca progress secara akurat tanpa *race condition* atau *dirty reads*, sementara background thread memperbaruinya.

---

## 2. Scalability: 50,000 Recipients

**Bagaimana Anda merancang sistem jika jumlah penerima mencapai 50.000 customer?**

Menampilkan dan memproses 50.000 penerima secara langsung di memory `@ViewScoped` akan menyebabkan *Out of Memory (OOM) Error* dan membebani browser client secara signifikan. Arsitektur harus diubah ke model **Decoupled & Persistent**:

### Langkah Skalabilitas:
1. **Lazy Loading & Pagination di UI:**
   Tidak menampilkan 50.000 row sekaligus. Kami akan mengubah `p:dataTable` menggunakan fitur **Lazy Loading** (`lazy="true"`). Data dimuat dari database secara bertahap per halaman (misal 50 row per page) menggunakan pagination SQL.
2. **Pecah Data dengan Batch Processing:**
   Background thread akan memproses data dalam bentuk batch (misal 500 data per batch) untuk membatasi konsumsi memori heap JVM.
3. **Pemisahan State ke Persistent Storage:**
   Progress broadcast tidak lagi disimpan dalam memori `@ViewScoped` bean (karena proses 50.000 pesan bisa memakan waktu berjam-jam dan session user bisa kedaluwarsa). Status broadcast akan disimpan di database relational atau cache terdistribusi (Redis).
4. **Message Queue (Decoupling):**
   Menggunakan arsitektur berbasis antrean (Message Queue seperti RabbitMQ, Apache Kafka, atau database queue). Ketika tombol "Start" diklik, sistem hanya memasukkan pesan-pesan tersebut ke dalam antrean (outbox). Proses pengiriman akan dijalankan oleh *worker service* terpisah secara asinkron di luar JVM web server (Microservices/Distributed Workers).
5. **Thread Pool Management:**
   Alih-alih membuat satu thread per request, worker menggunakan `ThreadPoolExecutor` yang dibatasi jumlah thread aktifnya (misal 10-20 thread) agar tidak melebihi kapasitas koneksi database atau API eksternal.

---

## 3. Resilience: Rate Limit Handling

**Bagaimana sistem menangani kesalahan Rate Limit (misal: WhatsApp API membatasi 30 pesan per detik)?**

Panggilan API eksternal harus dilindungi dengan mekanisme toleransi kesalahan (*fault tolerance*) agar tidak memicu pemblokiran akun atau kegagalan pengiriman massal:

### Strategi Ketahanan (*Resilience*):
1. **Internal Rate Limiter (Throttling):**
   Menerapkan pembatas kecepatan di sisi aplikasi (menggunakan algoritma *Token Bucket* atau library seperti *Resilience4j RateLimiter*). Kecepatan pengiriman dibatasi secara ketat di bawah limit WhatsApp (misal maks 25 pesan/detik).
2. **Retry dengan Exponential Backoff:**
   Jika menerima HTTP Status `429 Too Many Requests` dari API WhatsApp, worker tidak langsung menandai pesan sebagai `Failed`. Sistem akan menunggu selama $2^n$ detik (di mana $n$ adalah jumlah percobaan kembali) sebelum melakukan retry.
3. **Circuit Breaker Pattern:**
   Jika sistem menerima error Rate Limit atau Timeout secara berturut-turut melebihi batas toleransi (misal 10 kali gagal), Circuit Breaker akan berpindah ke state **OPEN** (menunda seluruh pengiriman secara otomatis selama beberapa menit) untuk memberi waktu bagi API pihak ketiga untuk pulih, sebelum mencoba mengirim kembali (state **HALF-OPEN**).
4. **Dead Letter Queue (DLQ):**
   Pesan yang telah melewati batas maksimal percobaan retry (misal 5 kali retry tetap gagal rate limit) akan dipindahkan ke DLQ (antrean kesalahan) untuk ditinjau secara manual oleh administrator atau dicoba ulang secara berkala nanti.
5. **Idempotency Key:**
   Setiap request ke API WhatsApp disertakan header ID transaksi unik. Jika terjadi retry pengiriman akibat network timeout, API penerima dapat mendeteksi duplikasi pesan dan mencegah pengiriman pesan ganda ke nomor customer yang sama.
