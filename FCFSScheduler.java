import java.io.*;
import java.util.*;

public class FCFSScheduler {
    public static void main(String[] args) {
        // Parametrik dosya adı (Varsayılan: odev1_case1.txt)
        String inputFile = (args.length > 0) ? args[0] : "odev1_case2.txt";
        String outputFile = "FCFS_sonuc.txt";
        
        List<Process> processes = readCSV(inputFile);
        if (processes.isEmpty()) return;

        // 1. FCFS'nin tek kuralı: Varış zamanına göre sırala
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            double currentTime = 0;
            int contextSwitches = 0;
            double contextSwitchTime = 0.001; //
            double totalBurstTime = processes.stream().mapToDouble(p -> p.burstTime).sum();
            
            int[] throughputTimes = {50, 100, 150, 200};
            int[] throughputCounts = new int[4];

            writer.println("--- a) Zaman Tablosu ---");

            for (int i = 0; i < processes.size(); i++) {
                Process p = processes.get(i);

                // IDLE Durumu: Eğer işlemci boşsa ve sıradaki süreç henüz gelmediyse
                if (currentTime < p.arrivalTime) {
                    writer.println("[" + currentTime + "]--IDLE--[" + p.arrivalTime + "]");
                    currentTime = p.arrivalTime;
                }

                // 2. Bağlam Değiştirme (İlk işlem hariç her geçişte ekle)
                // FCFS kesintisiz olduğu için her yeni iş bir "switch" sayılır
                if (i > 0) {
                    currentTime += contextSwitchTime;
                    contextSwitches++;
                }

                double startTime = currentTime;
                
                // Bekleme Süresi: Şu anki zaman - Geldiği zaman
                p.waitingTime = startTime - p.arrivalTime; 
                
                // İşletim (Burst Time kadar ileri git)
                currentTime += p.burstTime;
                
                // Bitiş ve Tamamlanma
                p.finishTime = currentTime;
                p.turnaroundTime = p.finishTime - p.arrivalTime;

                writer.println("[" + startTime + "]--" + p.id + "--[" + currentTime + "]");

                // Throughput Sayımı
                for (int t = 0; t < throughputTimes.length; t++) {
                    if (p.finishTime <= throughputTimes[t]) throughputCounts[t]++;
                }
            }

            // İstatistikleri Yazdır
            yazdirSonuclar(writer, processes, currentTime, totalBurstTime, contextSwitches, throughputCounts, throughputTimes);
            System.out.println("FCFS (" + inputFile + ") analizi tamamlandı.");

        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- YARDIMCI METOTLAR ---
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