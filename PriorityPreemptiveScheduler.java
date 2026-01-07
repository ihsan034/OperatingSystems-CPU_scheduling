import java.io.*;
import java.util.*;

public class PriorityPreemptiveScheduler {
    public static void main(String[] args) {
        String inputFile = (args.length > 0) ? args[0] : "odev1_case2.txt";
        String outputFile = "PriorityPreemptive_sonuc.txt";
        
        List<Process> processes = readCSV(inputFile);
        if (processes.isEmpty()) return;

        // Başlangıçta kalan süreleri burstTime'a eşitle
        for (Process p : processes) p.remainingTime = p.burstTime;

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            double currentTime = 0;
            int completed = 0;
            Process lastProcess = null;
            double segmentStartTime = 0;
            int contextSwitches = 0;
            double contextSwitchTime = 0.001; //

            double totalBurstTime = processes.stream().mapToDouble(p -> p.burstTime).sum();
            int[] throughputTimes = {50, 100, 150, 200};
            int[] throughputCounts = new int[4];

            writer.println("--- a) Zaman Tablosu ---");

            // Sonsuz döngü koruması
            while (completed < processes.size() && currentTime < 10000) {
                Process highestPriority = null;
                int bestPriorityVal = Integer.MAX_VALUE; // En küçük sayı = En yüksek öncelik

                // 1. O an gelmiş olanlar içinde EN YÜKSEK ÖNCELİĞİ bul
                for (Process p : processes) {
                    if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                        int pVal = getPriorityLevel(p.priority);
                        
                        // Daha yüksek öncelikli (sayıca küçük) veya
                        // Aynı öncelikte ama daha önce gelmiş (FCFS kuralı)
                        if (pVal < bestPriorityVal) {
                            bestPriorityVal = pVal;
                            highestPriority = p;
                        } else if (pVal == bestPriorityVal) {
                            if (highestPriority != null && p.arrivalTime < highestPriority.arrivalTime) {
                                highestPriority = p;
                            }
                        }
                    }
                }

                if (highestPriority == null) {
                    currentTime++;
                    continue;
                }

                // 2. Süreç Değişimi (Preemption)
                if (lastProcess != null && lastProcess != highestPriority) {
                    writer.println("[" + segmentStartTime + "]--" + lastProcess.id + "--[" + currentTime + "]");
                    currentTime += contextSwitchTime;
                    contextSwitches++;
                    segmentStartTime = currentTime;
                } else if (lastProcess == null) {
                    segmentStartTime = currentTime;
                }

                highestPriority.remainingTime--;
                currentTime++;
                lastProcess = highestPriority;

                // 3. İş bitti mi?
                if (highestPriority.remainingTime == 0) {
                    writer.println("[" + segmentStartTime + "]--" + highestPriority.id + "--[" + currentTime + "]");
                    highestPriority.finishTime = currentTime;
                    highestPriority.turnaroundTime = highestPriority.finishTime - highestPriority.arrivalTime;
                    highestPriority.waitingTime = highestPriority.turnaroundTime - highestPriority.burstTime;
                    completed++;
                    lastProcess = null;

                    for (int i = 0; i < throughputTimes.length; i++) {
                        if (highestPriority.finishTime <= throughputTimes[i]) throughputCounts[i]++;
                    }
                }
            }

            yazdirSonuclar(writer, processes, currentTime, totalBurstTime, contextSwitches, throughputCounts, throughputTimes);
            System.out.println("Priority Preemptive (" + inputFile + ") tamamlandı.");

        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- YARDIMCI METOTLAR ---
    private static int getPriorityLevel(String p) {
        switch (p.toLowerCase()) {
            case "high": return 1;
            case "normal": return 2;
            case "low": return 3;
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
