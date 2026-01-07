import java.io.*;
import java.util.*;

public class PreemptiveSJFScheduler {
    public static void main(String[] args) {
        String inputFile = "odev1_case2.txt"; // [cite: 35]
        String outputFile = "PreemptiveSJF_sonuc.txt"; // [cite: 16]
        List<Process> processes = readCSV(inputFile);

        if (processes.isEmpty()) {
            System.out.println("Dosya okunamadi!");
            return;
        }

        int n = processes.size();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            double currentTime = 0;
            int completed = 0;
            Process lastProcess = null;
            double segmentStartTime = 0;
            int contextSwitches = 0;
            double contextSwitchTime = 0.001; // [cite: 26]

            double totalBurstTime = processes.stream().mapToDouble(p -> p.burstTime).sum();
            int[] throughputTimes = {50, 100, 150, 200}; // [cite: 25]
            int[] throughputCounts = new int[4];

            writer.println("--- a) Zaman Tablosu ---"); // [cite: 17]

            // Sonsuz dongu koruması
            while (completed < n && currentTime < 10000) {
                Process shortest = null;
                int minRemaining = Integer.MAX_VALUE;

                for (Process p : processes) {
                    if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                        if (p.remainingTime < minRemaining) {
                            minRemaining = p.remainingTime;
                            shortest = p;
                        }
                    }
                }

                if (shortest == null) {
                    currentTime++;
                    continue;
                }

                if (lastProcess != null && lastProcess != shortest) {
                    writer.println("[" + (int)segmentStartTime + "]--" + lastProcess.id + "--[" + (int)currentTime + "]");
                    currentTime += contextSwitchTime;
                    contextSwitches++;
                    segmentStartTime = currentTime;
                } else if (lastProcess == null) {
                    segmentStartTime = currentTime;
                }

                shortest.remainingTime--;
                currentTime++;
                lastProcess = shortest;

                if (shortest.remainingTime == 0) {
                    writer.println("[" + (int)segmentStartTime + "]--" + shortest.id + "--[" + (int)currentTime + "]");
                    shortest.finishTime = currentTime;
                    shortest.turnaroundTime = shortest.finishTime - shortest.arrivalTime;
                    shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;
                    completed++;
                    lastProcess = null;

                    for (int i = 0; i < throughputTimes.length; i++) {
                        if (shortest.finishTime <= throughputTimes[i]) throughputCounts[i]++;
                    }
                }
            }

            // Eksik olan metodu burada cagiriyoruz
            yazdirIstatistikler(writer, processes, currentTime, totalBurstTime, contextSwitches, throughputCounts, throughputTimes);
            System.out.println("Preemptive SJF tamamlandi. Dosya: " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // HATA ALDIGIN 1. METOT:
    private static List<Process> readCSV(String filename) {
        List<Process> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                list.add(new Process(v[0], Integer.parseInt(v[1]), Integer.parseInt(v[2]), v[3]));
            }
        } catch (Exception e) {
            System.err.println("Okuma hatasi!");
        }
        return list;
    }

    // HATA ALDIGIN 2. METOT:
    private static void yazdirIstatistikler(PrintWriter writer, List<Process> processes, double currentTime, 
                                          double totalBurstTime, int contextSwitches, int[] throughputCounts, int[] throughputTimes) {
        double totalWait = 0, totalTurn = 0, maxWait = 0, maxTurn = 0;
        for (Process p : processes) {
            totalWait += p.waitingTime;
            totalTurn += p.turnaroundTime;
            maxWait = Math.max(maxWait, p.waitingTime);
            maxTurn = Math.max(maxTurn, p.turnaroundTime);
        }

        writer.println("\n--- b) Bekleme Suresi ---"); // [cite: 23]
        writer.printf("Maksimum: %.3f, Ortalama: %.3f\n", maxWait, (totalWait / processes.size()));

        writer.println("\n--- c) Tamamlanma Suresi ---"); // [cite: 24]
        writer.printf("Maksimum: %.3f, Ortalama: %.3f\n", maxTurn, (totalTurn / processes.size()));

        writer.println("\n--- d) Throughput ---"); // [cite: 25]
        for (int i = 0; i < throughputTimes.length; i++) {
            writer.println("T=" + throughputTimes[i] + " için: " + throughputCounts[i]);
        }

        writer.println("\n--- e) Ortalama CPU Verimliligi ---"); // [cite: 26]
        writer.printf("Verimlilik: %%%.4f\n", (totalBurstTime / currentTime) * 100);

        writer.println("\n--- f) Toplam Baglam Degistirme Sayisi ---"); // [cite: 26]
        writer.println("Sayı: " + contextSwitches);
    }
}