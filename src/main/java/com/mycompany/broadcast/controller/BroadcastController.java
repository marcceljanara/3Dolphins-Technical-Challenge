package com.mycompany.broadcast.controller;

import com.mycompany.broadcast.model.Customer;
import com.mycompany.broadcast.service.IntegrationService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Named("broadcastController")
@ViewScoped
public class BroadcastController implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Customer> customers;
    private transient IntegrationService integrationService;
    private transient ExecutorService executorService;

    private volatile boolean broadcastRunning;
    private AtomicInteger progress;
    private AtomicInteger processedCount;
    private AtomicInteger successCount;
    private AtomicInteger failedCount;
    private int totalSelected;

    @PostConstruct
    public void init() {
        integrationService = new IntegrationService();
        customers = new ArrayList<>();
        progress = new AtomicInteger(0);
        processedCount = new AtomicInteger(0);
        successCount = new AtomicInteger(0);
        failedCount = new AtomicInteger(0);
        broadcastRunning = false;
        completedNotificationSent = false;
        totalSelected = 0;

        // Inisialisasi 10 mock customer sesuai spesifikasi issues.md
        customers.add(new Customer(1L, "Budi Santoso",    "+6281234567801"));
        customers.add(new Customer(2L, "Siti Rahayu",     "+6281234567802"));
        customers.add(new Customer(3L, "Agus Pratama",    "+6281234567803"));
        customers.add(new Customer(4L, "Dewi Lestari",    "+6281234567804"));
        customers.add(new Customer(5L, "Eko Wijaya",      "+6281234567805"));
        customers.add(new Customer(6L, "Fitri Handayani", "+6281234567806"));
        customers.add(new Customer(7L, "Gunawan Putra",   "+6281234567807"));
        customers.add(new Customer(8L, "Hana Permata",    "+6281234567808"));
        customers.add(new Customer(9L, "Irfan Maulana",   "+6281234567809"));
        customers.add(new Customer(10L,"Joko Widodo",     "+6281234567810"));
    }

    private volatile boolean completedNotificationSent;

    public void startBroadcast() {
        // 1. Filter customer yang terpilih (selected == true)
        List<Customer> selectedCustomers = customers.stream()
                .filter(Customer::isSelected)
                .collect(Collectors.toList());

        // 2. Validasi minimal 1 customer terpilih
        if (selectedCustomers.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validasi Gagal", "Pilih minimal 1 customer untuk memulai broadcast!"));
            return;
        }

        // 3. Reset stats & set status pengiriman awal
        broadcastRunning = true;
        progress.set(0);
        processedCount.set(0);
        successCount.set(0);
        failedCount.set(0);
        totalSelected = selectedCustomers.size();
        completedNotificationSent = false;

        for (Customer c : selectedCustomers) {
            c.setStatus("Queued");
        }

        // 4. Jalankan broadcast di background thread
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        executorService.submit(() -> {
            try {
                for (Customer customer : selectedCustomers) {
                    // Cek jika thread disela
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    // a. Update status ke "Sending"
                    synchronized (customer) {
                        customer.setStatus("Sending");
                    }

                    // b. Panggil IntegrationService (mengandung latency 1-2s & failure rate 10%)
                    boolean success = integrationService.sendMessage(customer);

                    // c. Update status berdasarkan hasil API
                    synchronized (customer) {
                        if (success) {
                            customer.setStatus("Sent");
                            successCount.incrementAndGet();
                        } else {
                            customer.setStatus("Failed");
                            failedCount.incrementAndGet();
                        }
                    }

                    // d. Update counter dan progress
                    int processed = processedCount.incrementAndGet();
                    progress.set((processed * 100) / totalSelected);
                }
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(BroadcastController.class.getName())
                        .severe("Terjadi kesalahan pada background broadcast: " + e.getMessage());
            } finally {
                broadcastRunning = false;
            }
        });

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Broadcast Dimulai", "Mengirim broadcast ke " + totalSelected + " penerima..."));
    }

    public void onBroadcastComplete() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Selesai", "Priority Broadcast selesai diproses!"));
    }

    public void checkProgress() {
        if (!broadcastRunning && totalSelected > 0 && !completedNotificationSent) {
            completedNotificationSent = true;
            onBroadcastComplete();
        }
    }

    public void resetBroadcast() {
        // Pastikan tidak ada pengiriman aktif
        if (broadcastRunning) {
            return;
        }

        progress.set(0);
        processedCount.set(0);
        successCount.set(0);
        failedCount.set(0);
        totalSelected = 0;
        completedNotificationSent = false;

        for (Customer c : customers) {
            c.setSelected(false);
            c.setStatus("Pending");
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Reset Berhasil", "Semua status customer dan progress telah di-reset."));
    }

    @PreDestroy
    public void cleanup() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    // Getter & Setter untuk JSF EL Expression
    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public boolean isBroadcastRunning() {
        return broadcastRunning;
    }

    public int getProgress() {
        return progress.get();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public int getTotalSelected() {
        return totalSelected;
    }
}
