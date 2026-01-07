# 📊 CPU Zamanlama Algoritmaları - Performans Analiz Raporu

Bu rapor Case 1 ve Case 2 veri setleri üzerinde simüle edilen 6 farklı CPU zamanlama algoritmasının performans metriklerini detaylandırıyor.

---

## 1. BÖLÜM: CASE 1 ANALİZİ (200 Süreç)

### b, c, e, f) Performans Metrikleri Tablosu

| Algoritma | Ort/Maks Bekleme (b) | Ort/Maks Tamamlama (c) | Verimlilik (e) | Bağlam Değişimi (f) |
| :--- | :---: | :---: | :---: | :---: |
| **FCFS** | 813.6 / 1683.2 ms | 824.1 / 1703.2 ms | %99.94 | 199 |
| **SJF (Non-Preemptive)** | 537.5 / 1863.2 ms | 548.0 / 1883.2 ms | %99.94 | 199 |
| **SJF (Preemptive)** | 537.0 / 1863.0 ms | 547.5 / 1883.0 ms | %99.95 | 13 |
| **Round Robin (Q=2)** | 1092.1 / 1863.6 ms | 1102.6 / 1883.6 ms | %99.92 | 599 |
| **Priority (Non-P.)** | 824.9 / 1689.2 ms | 835.4 / 1707.2 ms | %99.94 | 199 |
| **Priority (Preemptive)** | 833.6 / 1689.0 ms | 844.1 / 1707.0 ms | %99.95 | 2 |


### d) Throughput (Birim İş) Analizi

| Algoritma | T=500 ms | T=1000 ms | T=1500 ms | T=2000 ms |
| :--- | :---: | :---: | :---: | :---: |
| **FCFS** | 52 | 97 | 147 | 194 |
| **SJF (Non-P.)** | 94 | 136 | 168 | 194 |
| **SJF (Preemptive)** | **94** | **136** | **168** | **194** |
| **Round Robin (Q=2)** | 32 | 66 | 100 | 168 |
| **Priority (Non-P.)** | 49 | 96 | 144 | 191 |
| **Priority (Preemp.)** | 49 | 96 | 144 | 191 |

---

## 2. BÖLÜM: CASE 2 ANALİZİ (100 Süreç)

### b, c, e, f) Performans Metrikleri Tablosu

| Algoritma | Ort/Maks Bekleme (b) | Ort/Maks Tamamlama (c) | Verimlilik (e) | Bağlam Değişimi (f) |
| :--- | :---: | :---: | :---: | :---: |
| **FCFS** | 418.0 / 851.1 ms | 428.5 / 853.1 ms | %99.99 | 99 |
| **SJF (Non-Preemptive)** | 268.4 / 926.1 ms | 278.9 / 946.1 ms | %99.99 | 99 |
| **SJF (Preemptive)** | 267.9 / 926.0 ms | 278.4 / 946.0 ms | %99.99 | 11 |
| **Round Robin (Q=2)** | 551.1 / 926.3 ms | 561.6 / 944.3 ms | %99.97 | 299 |
| **Priority (Non-P.)** | 409.7 / 836.1 ms | 420.2 / 854.1 ms | %99.99 | 99 |
| **Priority (Preemptive)** | 411.4 / 836.0 ms | 421.9 / 854.0 ms | %99.99 | 1 |

### d) Throughput (Birim İş) Analizi

| Algoritma | T=250 ms | T=500 ms | T=750 ms | T=1000 ms |
| :--- | :---: | :---: | :---: | :---: |
| **FCFS** | 24 | 48 | 71 | 95 |
| **SJF (Non-P.)** | 47 | 68 | 84 | 97 |
| **SJF (Preemptive)** | **47** | **68** | **84** | **97** |
| **Round Robin (Q=2)** | 15 | 31 | 51 | 84 |
| **Priority (Non-P.)** | 25 | 50 | 72 | 96 |
| **Priority (Preemp.)** | 24 | 50 | 72 | 96 |

---
## 🗨️ Ayrıntılı Birkaç Yorumum

**Görüldüğü üzere CPU verimliliği algoritmalarda birbirine çok yakın çıktı bunun sebebi ödevdeki girilen parametreler ve Bağlam Değiştirme Süresi: 0,001 birim zaman alması ile alakalı yani bağlam değiştirme çok fazla olsa bile (1000) gibi 0.001 birim zaman verimliliği çok etkileyemiyor tabi şunu da bilmek lazım CPU'da ki verimlilik azalışı sayısal olarak o kadar az gözükse bile aslında bilgisayarda verimlilik azalışı sayılardan çok daha öteye gidebilir** 


**Case 2 senaryosunda Case 1’den farklı olarak Priority algoritmalarının FCFS’den daha iyi performans sergilediği gözlemlenmiştir. Bu durum veri setindeki öncelik dağılımının iş yükü ile daha uyumlu olduğunu göstermektedir. Case 2 veri setindeki yüksek öncelikli süreçlerin aynı zamanda nispeten kısa veya sistem akışını bozmayacak bir sırada geldiğini gösterir. Yani "Öncelik" bu senaryoda sistemi yavaşlatmak yerine hızlandırmış.**


**Priority Preemptive algoritmasının sadece 1 (bir) adet bağlam değişimi yapması.1 defa bağlam değişimi yapılması bize ya yüksek öcelikli işlerin en baştan geldiğini ya da işemcinin boşken geldiğini gösteriyor bu veri case2'nin priority algoritmaları için kesintisiz geçtiğini gösteriyor**


**Burada bana göre en önemli sayılabilecek yorum performans sonuçları ne olursa olsun bu sonuçların girilen parametredeki değerler (örneğin round robin'deki time quantum (q) değeri ya da işlemciye gelen işlemin hangi anda geldiği, işlem süresi, önceliği vs.) değişirse bu algoritmalar bize çok farklı şekilde sonuçlar verebilir mesela en iyi performansı gösteren algoritma en kötüye düşebilir**


---

## 📝 Genel Sonuç Yorumlanması

1. **SJF Üstünlüğü:** Her iki senaryoda da SJF (özellikle Preemptive versiyonu) bekleme sürelerini minimize etme ve Throughput (iş bitirme hızı) açısından açık ara en başarılı algoritmadır. 
2. **Round Robin ve Kuantum Etkisi:** Round Robin algoritması Case 1'de 599 Case 2'de 299 bağlam değişimi yaparak sisteme en çok yükü bindiren algoritma olmuştur. Bu durum batch (toplu) işlemlerde RR'nin verimsizliğini kanıtlar.
3. **Priority Algoritmaları:** Case 2'de öncelikli algoritmaların FCFS'den daha iyi sonuç vermesi bu veri setindeki öncelik dağılımının sistem akışına daha uygun olduğunu gösterir.

---

## 🎓 Kendi Genel Analiz Notum
Bu rapordaki tüm sayısal veriler, simülasyon çıktılarından manuel olarak tablolaştırılmıştır. Analizler, algoritmaların teorik davranışlarının simülasyon ortamındaki karşılıklarını yansıtmaktadır.
