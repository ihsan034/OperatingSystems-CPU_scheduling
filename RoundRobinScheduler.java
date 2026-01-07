import java.io.*;
import java.util.*;

public class RoundRobinScheduler {
    public static void main(String[] args) {
        // 1. Parametreleri Al: Dosya Adı ve Quantum Değeri (Varsayılan q=2)
        String inputFile = (args.length > 0) ? args[0] : "odev1_case2.txt";
        int quantum = (args.length > 1) ? Integer.parseInt(args[1]) : 4 ; 
        
        String outputFile = "RoundRobin_sonuc.txt";
        
        List<Process> processes = readCSV(inputFile);
        if (processes.isEmpty()) return;

        // Başlangıçta kalan süreleri burstTime'a eşitle
        for (Process p : processes) p.remainingTime = p.burstTime;

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            double currentTime = 0;
            int completed = 0;
            int contextSwitches = 0;
            double contextSwitchTime = 0.001; //
            double totalBurstTime = processes.stream().mapToDouble(p -> p.burstTime).sum();
            
            int[] throughputTimes = {50, 100, 150, 200};
            int[] throughputCounts = new int[4];
            
            Queue<Process> readyQueue = new LinkedList<>();
            Process lastProcess = null;
            
            // Süreçleri varış zamanına göre sırala
            processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
            int pIndex = 0; // Hangi süreçte kaldığımızı takip eder

            writer.println("--- a) Zaman Tablosu ---");

            while (completed < processes.size()) {
                // 1. Mevcut zamana kadar gelmiş yeni süreçleri kuyruğa ekle
                while (pIndex < processes.size() && processes.get(pIndex).arrivalTime <= currentTime) {
                    readyQueue.add(processes.get(pIndex));
                    pIndex++;
                }

                if (readyQueue.isEmpty()) {
                    // Kuyruk boşsa bir sonraki sürecin gelişine atla (IDLE)
                    if (pIndex < processes.size()) {
                        double nextArrival = processes.get(pIndex).arrivalTime;
                        writer.println("[" + currentTime + "]--IDLE--[" + nextArrival + "]");
                        currentTime = nextArrival;
                    }
                    continue;
                }

                Process currentProcess = readyQueue.poll();

                // 2. Bağlam Değiştirme
                if (lastProcess != null && lastProcess != currentProcess) {
                    currentTime += contextSwitchTime;
                    contextSwitches++;
                }

                double startTime = currentTime;
                
                // 3. Çalıştırma (Quantum kadar mı yoksa kalan süre kadar mı?)
                int timeToRun = Math.min(currentProcess.remainingTime, quantum);
                
                currentProcess.remainingTime -= timeToRun;
                currentTime += timeToRun;
                lastProcess = currentProcess;

                writer.println("[" + startTime + "]--" + currentProcess.id + "--[" + currentTime + "]");

                // 4. Çalışırken yeni gelenler oldu mu? (Önce yenileri ekle)
                while (pIndex < processes.size() && processes.get(pIndex).arrivalTime <= currentTime) {
                    readyQueue.add(processes.get(pIndex));
                    pIndex++;
                }

                // 5. İş bitmediyse kuyruğun sonuna geri ekle
                if (currentProcess.remainingTime > 0) {
                    readyQueue.add(currentProcess);
                } else {
                    // İş bitti
                    completed++;
                    currentProcess.finishTime = currentTime;
                    currentProcess.turnaroundTime = currentProcess.finishTime - currentProcess.arrivalTime;
                    currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                    
                    for (int i = 0; i < throughputTimes.length; i++) {
                        if (currentProcess.finishTime <= throughputTimes[i]) throughputCounts[i]++;
                    }
                }
            }

            yazdirSonuclar(writer, processes, currentTime, totalBurstTime, contextSwitches, throughputCounts, throughputTimes);
            System.out.println("Round Robin (" + inputFile + ", q=" + quantum + ") tamamlandı.");

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