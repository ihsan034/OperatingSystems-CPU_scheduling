import java.io.*;
import java.util.*;

public class NonPreemptiveSJFScheduler {
    public static void main(String[] args) {
        // Parametrik giriş: Terminalden gelen dosya adını al, yoksa varsayılanı kullan
        String inputFile = (args.length > 0) ? args[0] : "odev1_case2.txt"; 
        String outputFile = "NonPreemptiveSJF_sonuc.txt";
        
        List<Process> processes = readCSV(inputFile);
        if (processes.isEmpty()) return;

        int n = processes.size();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            double currentTime = 0;
            int completed = 0;
            int contextSwitches = 0;
            double contextSwitchTime = 0.001; //
            double totalBurstTime = processes.stream().mapToDouble(p -> p.burstTime).sum();
            
            int[] throughputTimes = {50, 100, 150, 200}; //
            int[] throughputCounts = new int[4];
            List<Process> readyQueue = new ArrayList<>();

            writer.println("--- a) Zaman Tablosu ---"); //

            while (completed < n) {
                // 1. Şu anki zamana kadar gelen tüm süreçleri kuyruğa ekle
                for (Process p : processes) {
                    if (p.arrivalTime <= currentTime && p.finishTime == 0 && !readyQueue.contains(p)) {
                        readyQueue.add(p);
                    }
                }

                if (readyQueue.isEmpty()) {
                    currentTime++; // Kimse yoksa zamanı ilerlet (IDLE)
                    continue;
                }

                // 2. Hazır olanlar içinden Burst Time'ı EN KISA olanı seç (Kesintisiz)
                readyQueue.sort(Comparator.comparingInt(p -> p.burstTime));
                Process shortest = readyQueue.remove(0);

                // 3. Bağlam Değiştirme (Context Switch) - İlk işlem hariç
                if (currentTime > 0) {
                    currentTime += contextSwitchTime;
                    contextSwitches++;
                }

                // 4. İşlemi çalıştır (Yarıda kesme yok)
                double startTime = currentTime;
                shortest.waitingTime = currentTime - shortest.arrivalTime;
                currentTime += shortest.burstTime;
                shortest.finishTime = currentTime;
                shortest.turnaroundTime = shortest.finishTime - shortest.arrivalTime;
                
                // Zaman tablosuna yaz
                writer.println("[" + startTime + "]--" + shortest.id + "--[" + currentTime + "]");
                completed++;

                // Throughput hesabı
                for (int i = 0; i < throughputTimes.length; i++) {
                    if (shortest.finishTime <= throughputTimes[i]) throughputCounts[i]++;
                }
            }

            // b, c, d, e, f maddelerini yazdır
            yazdirSonuclar(writer, processes, currentTime, totalBurstTime, contextSwitches, throughputCounts, throughputTimes);
            System.out.println(inputFile + " için Non-Preemptive SJF tamamlandı.");

        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- EKSİK OLAN METOTLAR ---

    private static List<Process> readCSV(String filename) {
        List<Process> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Başlık satırı
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                list.add(new Process(v[0], Integer.parseInt(v[1]), Integer.parseInt(v[2]), v[3]));
            }
        } catch (Exception e) { System.err.println("Okuma hatasi: " + filename); }
        return list;
    }

    private static void yazdirSonuclar(PrintWriter writer, List<Process> processes, double currentTime, 
                                      double totalBurstTime, int contextSwitches, int[] throughputCounts, int[] throughputTimes) {
        double totalWait = 0, totalTurn = 0, maxWait = 0, maxTurn = 0;
        for (Process p : processes) {
            totalWait += p.waitingTime;
            totalTurn += p.turnaroundTime;
            maxWait = Math.max(maxWait, p.waitingTime);
            maxTurn = Math.max(maxTurn, p.turnaroundTime);
        }

        writer.println("\n--- b) Bekleme Süresi ---"); //
        writer.printf("Maksimum: %.3f, Ortalama: %.3f\n", maxWait, (totalWait / processes.size()));

        writer.println("\n--- c) Tamamlanma Süresi ---"); //
        writer.printf("Maksimum: %.3f, Ortalama: %.3f\n", maxTurn, (totalTurn / processes.size()));

        writer.println("\n--- d) Throughput ---"); //
        for (int i = 0; i < throughputTimes.length; i++) {
            writer.println("T=" + throughputTimes[i] + " için: " + throughputCounts[i]);
        }

        writer.println("\n--- e) Ortalama CPU Verimliliği ---"); //
        writer.printf("Verimlilik: %%%.4f\n", (totalBurstTime / currentTime) * 100);

        writer.println("\n--- f) Toplam Bağlam Değiştirme Sayısı ---"); //
        writer.println("Sayı: " + contextSwitches);
    }
}