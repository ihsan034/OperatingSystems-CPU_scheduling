import java.io.*;
import java.util.*;

public class PriorityNonPreemptiveScheduler {
    public static void main(String[] args) {
        String inputFile = (args.length > 0) ? args[0] : "odev1_case2.txt";
        String outputFile = "PriorityNonPreemptive_sonuc.txt";
        
        List<Process> processes = readCSV(inputFile);
        if (processes.isEmpty()) return;

        int n = processes.size();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            double currentTime = 0;
            int completed = 0;
            int contextSwitches = 0;
            double contextSwitchTime = 0.001;
            double totalBurstTime = processes.stream().mapToDouble(p -> p.burstTime).sum();
            
            int[] throughputTimes = {50, 100, 150, 200};
            int[] throughputCounts = new int[4];
            
            List<Process> readyQueue = new ArrayList<>();

            writer.println("--- a) Zaman Tablosu ---");

            while (completed < n) {
                // 1. Gelenleri kuyruğa ekle
                for (Process p : processes) {
                    if (p.arrivalTime <= currentTime && p.finishTime == 0 && !readyQueue.contains(p)) {
                        readyQueue.add(p);
                    }
                }

                if (readyQueue.isEmpty()) {
                    currentTime++;
                    continue;
                }

                // 2. KRİTİK NOKTA: Önceliğe göre sırala (High=1 < Normal=2 < Low=3)
                // Eğer öncelikler eşitse, geliş zamanına bak (FCFS mantığı)
                readyQueue.sort((p1, p2) -> {
                    int prio1 = getPriorityLevel(p1.priority);
                    int prio2 = getPriorityLevel(p2.priority);
                    if (prio1 != prio2) {
                        return Integer.compare(prio1, prio2); // Küçük sayı = Yüksek öncelik
                    } else {
                        return Integer.compare(p1.arrivalTime, p2.arrivalTime); // Eşitse önce gelen
                    }
                });

                Process current = readyQueue.remove(0);

                // 3. Bağlam Değiştirme
                if (currentTime > 0) {
                    currentTime += contextSwitchTime;
                    contextSwitches++;
                }

                // 4. İşlemi Yap (Kesintisiz)
                double startTime = currentTime;
                current.waitingTime = currentTime - current.arrivalTime;
                currentTime += current.burstTime;
                
                current.finishTime = currentTime;
                current.turnaroundTime = current.finishTime - current.arrivalTime;
                
                writer.println("[" + startTime + "]--" + current.id + "--[" + currentTime + "]");
                completed++;

                 // Throughput
                 for (int i = 0; i < throughputTimes.length; i++) {
                    if (current.finishTime <= throughputTimes[i]) throughputCounts[i]++;
                }
            }

            yazdirSonuclar(writer, processes, currentTime, totalBurstTime, contextSwitches, throughputCounts, throughputTimes);
            System.out.println("Priority Non-Preemptive (" + inputFile + ") tamamlandı.");

        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- YARDIMCI METOTLAR ---

    // String önceliği sayıya çeviren metot
    private static int getPriorityLevel(String p) {
        switch (p.toLowerCase()) {
            case "high": return 1;   // En yüksek öncelik
            case "normal": return 2;
            case "low": return 3;    // En düşük öncelik
            default: return 4;
        }
    }

    private static List<Process> readCSV(String filename) {
        List<Process> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                list.add(new Process(v[0], Integer.parseInt(v[1]), Integer.parseInt(v[2]), v[3]));
            }
        } catch (Exception e) { System.err.println("Dosya okuma hatasi: " + filename); }
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

        writer.println("\n--- b) Bekleme Süresi ---");
        writer.printf("Maksimum: %.3f, Ortalama: %.3f\n", maxWait, (totalWait / processes.size()));

        writer.println("\n--- c) Tamamlanma Süresi ---");
        writer.printf("Maksimum: %.3f, Ortalama: %.3f\n", maxTurn, (totalTurn / processes.size()));

        writer.println("\n--- d) Throughput ---");
        for (int i = 0; i < throughputTimes.length; i++) {
            writer.println("T=" + throughputTimes[i] + " için: " + throughputCounts[i]);
        }

        writer.println("\n--- e) Ortalama CPU Verimliliği ---");
        writer.printf("Verimlilik: %%%.4f\n", (totalBurstTime / currentTime) * 100);

        writer.println("\n--- f) Toplam Bağlam Değiştirme Sayısı ---");
        writer.println("Sayı: " + contextSwitches);
    }
}