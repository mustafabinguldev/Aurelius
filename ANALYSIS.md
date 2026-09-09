# Proje incelemesi — 9 Eylül 2026

İnceleme kapsamı: Maven derlemesi, HTTP yönlendirme, HTML ve medya yanıtları,
uygulama başlangıcı, YAML okuma ve eklenti yükleme. Bu çalışma tam güvenlik
denetimi veya yük testi değildir.

## Düzeltilen sorunlar

| Sorun | Sonuç | Kanıt |
| --- | --- | --- |
| Olmayan REST endpoint'i HTTP 200 dönüyordu | HTTP 404 dönüyor | `NettyRestFulHandlerTest#missingRouteReturns404` |
| `/api/usersExtra`, `users` endpoint'iyle eşleşiyordu | Eşleşme yol segmenti sınırını denetliyor | `NettyRestFulHandlerTest#routePrefixMustEndAtSegmentBoundary` |
| Aynı yoldaki GET kaydı POST kaydını gölgeleyebiliyordu | En özgül yolun kayıtları arasından HTTP metoduna göre seçim yapılıyor | `NettyRestFulHandlerTest#samePathCanHaveMultipleMethods` |
| Desteklenmeyen metot HTTP 400 dönüyordu | HTTP 405 ve desteklenen metotları gösteren `Allow` başlığı dönüyor | `NettyRestFulHandlerTest#unsupportedMethodReturns405AndAllow` |
| İç içe endpoint'lerde endpoint'in bir kısmı parametrelere ekleniyordu | Parametreler eşleşen endpoint'ten sonraki segmentlerden oluşuyor | `NettyRestFulHandlerTest#nestedRouteReceivesOnlyRemainingSegments` |
| Varsayılan ve özel HTML 404 sayfaları HTTP 200 dönüyordu | İki hata yanıtı da HTTP 404 dönüyor | `NettyResponseMvcHandlerTest` |
| `.gitignore` bütün test dizinlerini dışlıyordu | Testler sürüm kontrolüne dahil edilebilir; JUnit ve Mockito test bağımlılıkları eklendi | `.gitignore`, `pom.xml`, `src/test/java` |

REST yönlendirici artık global uygulama yerine `NettyResponseService` içindeki
uygulamayı kullanıyor. En özgül yol metodu desteklemiyorsa üst endpoint'e
geri dönmüyor; bu davranış ayrı regresyon testiyle korunuyor.

## Kalan bulgular ve öncelikleri

Aşağıdakiler kaynak kod incelemesi bulgularıdır; bu değişiklikte düzeltilmedi.

| Öncelik | Bulgu ve etkisi | Kaynak / önerilen çalışma |
| --- | --- | --- |
| Yüksek | REST yanıtlarında oluşturulan geçici `ByteBuf`, `writeBytes(buf)` sonrasında serbest bırakılmıyor. Tekrarlanan isteklerde referans sayımlı kaynak birikimi riski var. | `src/main/java/tech/bingulhan/webserver/response/impl/restful/impl/` altındaki dört handler; doğrudan yanıt tamponuna yazma ve kaynak yaşam döngüsü testleri. |
| Yüksek | Medya dosyasının tamamı Netty istek iş parçacığında senkron okunuyor. Büyük dosyalar bellek kullanımını artırıp diğer istekleri geciktirebilir. | `NettyResponseMediaHandler#handleResponse`; akış tabanlı aktarım ve eşzamanlı büyük dosya testi. |
| Orta | `threadSize` ayarı okunuyor fakat worker sayısı işlemci sayısından hesaplanıyor. | `AureliusApplication#readSettingsYml`, `HttpNettyServer#start`; ayar doğrulaması ve yaşam döngüsü testleri. |
| Orta | YAML giriş akışı kapatılmıyor; eksik veya yanlış tipli ayarlar doğrudan cast ediliyor. | `AureliusApplication#readYaml`, `readSettingsYml`; try-with-resources ve bozuk yapılandırma testleri. |
| Orta | Eklenti yükleme hataları sessizce yutuluyor; sınıf yükleyici ve YAML akışının kapanışı yönetilmiyor. | `FileAddonCompiler#doCompileAllAddons`, `registerAddon`; eklenti yaşam döngüsüne bağlı kapatma ve hata raporlama. |
| Orta | `shutdown()` bütün JVM'i kapatıyor; kesinti işareti geri yüklenmiyor; başlangıç hatasında executor temizliği eksik. | `HttpNettyServer`; gömülü kullanım, başlatma hatası ve durdurma testleri. |
| Orta | Lombok ve annotations sürümleri `RELEASE`; aynı kaynak ileride farklı bağımlılıklarla derlenebilir. | `pom.xml`; sürümleri sabitleme ve bağımlılık güvenlik taraması. Güncel CVE taraması yapılmadı. |
| Orta | README Java 8+ diyor, POM JavaFX 17 kullanıyor; Java 8 uyumluluğu doğrulanmış değil. | `README.MD`, `pom.xml`; desteklenen JDK matrisiyle CI. Bu çalışmada JDK 17 kullanıldı. |

## Doğrulama

- Başlangıç derlemesi JDK 17 ve Maven 3.8.5 ile geçti; başlangıçta test yoktu.
- REST düzeltmesinden önce 7 testin 6'sı beklenen davranış farklarıyla başarısız oldu.
- HTML düzeltmesinden önce 3 testin 2'si `expected 404 but was 200` ile başarısız oldu.
- Düzeltmelerden sonra Maven `verify` geçti: 10 test, 0 hata, 0 başarısızlık; JAR paketlendi.
- Testler Netty `EmbeddedChannel` üzerinden gerçek handler yanıtlarını denetliyor.
  Uygulama başlangıcı/disk erişimi taklit ediliyor; canlı soket, JavaFX arayüzü,
  eklenti entegrasyonu ve yük altında çalışma sınanmadı.

Yerel çalıştırma: JDK 17 ile `mvn test`; paketleme için `mvn verify`.
Maven bu ortamda PATH üzerinde değildi; mevcut `.m2/wrapper/dists` kurulumu kullanıldı.
