package com.mycompany.broadcast.service;

import com.mycompany.broadcast.model.Customer;
import java.util.Random;
import java.util.logging.Logger;

public class IntegrationService {

    private static final Logger LOG = Logger.getLogger(IntegrationService.class.getName());
    private final Random random = new Random();

    /**
     * Simulasi pengiriman pesan ke API eksternal (misal WhatsApp).
     * - Delay: 1-2 detik (simulasi network latency)
     * - Failure rate: 10%
     *
     * @param customer target customer
     * @return true jika berhasil, false jika gagal
     */
    public boolean sendMessage(Customer customer) {
        try {
            // Simulasi network latency (1000-2000ms)
            int delay = 1000 + random.nextInt(1001); // 1000 sampai 2000
            LOG.info("Mengirim pesan ke " + customer.getName() 
                     + " (" + customer.getPhoneNumber() + ")... delay=" + delay + "ms");
            Thread.sleep(delay);

            // Simulasi 10% failure rate
            boolean success = random.nextInt(100) >= 10; // 90% success
            
            if (success) {
                LOG.info("✅ Pesan ke " + customer.getName() + " BERHASIL dikirim.");
            } else {
                LOG.warning("❌ Pesan ke " + customer.getName() + " GAGAL (simulasi API error).");
            }
            
            return success;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.severe("Thread interrupted saat mengirim ke " + customer.getName());
            return false;
        }
    }
}
