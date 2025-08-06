# 🎁 GiftGenie - Yapay Zeka Destekli Hediye Önerici

GiftGenie, yapay zeka teknolojisi ile kişiselleştirilmiş hediye önerileri sunan modern bir Android uygulamasıdır. Kullanıcılar sadece birkaç basit soruyu cevaplayarak, sevdikleri için mükemmel hediyeleri bulabilirler.

## ✨ Özellikler

- 🤖 **Yapay Zeka Destekli Öneriler**: Groq API kullanarak akıllı hediye önerileri
- 📱 **Modern UI/UX**: Material Design 3 ve Jetpack Compose ile tasarlanmış
- 🎯 **Kişiselleştirilmiş Sonuçlar**: 15 detaylı soru ile tam kişiselleştirme
- 💫 **Güzel Animasyonlar**: Akıcı geçişler ve yükleme animasyonları
- 🛒 **Alışveriş Entegrasyonu**: Önerilen hediyeler için direkt satın alma linkleri
- 🔄 **Çoklu Kullanım**: Farklı kişiler için tekrar tekrar kullanım

## 📋 Soru Kategorileri

Uygulama aşağıdaki 15 kategoride soru sorarak en uygun hediye önerilerini sunar:

1. **İlişki Türü** - Aile, sevgili, arkadaş, iş arkadaşı vb.
2. **Cinsiyet** - Kadın, erkek, belirtmek istemiyorum
3. **Yaş Grubu** - 0-12, 13-17, 18-25, 26-35, 36-50, 50+
4. **Meslek** - Öğrenci, akademisyen, yazılımcı, sağlık çalışanı vb.
5. **Hobi ve İlgi Alanları** - Kitap, spor, müzik, sanat vb. (çoklu seçim)
6. **Teknoloji İlgisi** - Çok ilgili, orta, az
7. **Sanat İlgisi** - Evet, hayır, bilmiyorum
8. **Kişilik Tipi** - Enerjik, romantik, duygusal, sosyal vb.
9. **Moda İlgisi** - Evet, hayır, bilmiyorum
10. **Stil Tercihi** - Minimalist, gösterişli, karışık
11. **Önceki Hediyeler** - Daha önce alınan hediyeler (metin girişi)
12. **Hediye Amacı** - Doğum günü, sevgililer günü, sürpriz vb.
13. **Teslimat Hızı** - Acil, farketmez
14. **Hediye Türü** - Fiziksel, dijital, farketmez
15. **Bütçe** - 0-100 TL, 100-250 TL, 250-500 TL, 500-1000 TL, 1000 TL+

## 🏗️ Mimari

Proje modern Android geliştirme prensiplerine uygun olarak tasarlanmıştır:

### Teknolojiler
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Navigation Compose
- **Network**: Retrofit + OkHttp + Gson
- **State Management**: StateFlow + Compose State
- **Asynchronous**: Kotlin Coroutines
- **Animation**: Compose Animation APIs

### Katman Yapısı
```
app/
├── data/           # Data sınıfları ve modeller
├── network/        # API servisleri ve network konfigürasyonu
├── repository/     # Veri katmanı ve business logic
├── viewmodel/      # UI state yönetimi
├── ui/
│   ├── screens/    # Compose ekranları
│   └── theme/      # Tema ve stil tanımlamaları
└── navigation/     # Navigation graph ve routing
```

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler
- Android Studio Arctic Fox (2020.3.1) veya daha yeni
- Android SDK API Level 24 (Android 7.0) veya üstü
- Kotlin 2.0.21
- Gradle 8.9.1

### Adımlar
1. **Projeyi Klonlayın**
   ```bash
   git clone [repository-url]
   cd hediyeApp
   ```

2. **Android Studio'da Açın**
   - Android Studio'yu açın
   - "Open an existing project" seçin
   - Proje klasörünü seçin

3. **Dependencies'leri Sync Edin**
   - Android Studio otomatik olarak gradle sync yapacaktır
   - Veya manuel olarak: `File > Sync Project with Gradle Files`

4. **API Key Setup**
   - `ApiService.kt` dosyasında `YOUR_GROQ_API_KEY_HERE` placeholder'ını kendi Groq API key'iniz ile değiştirin
   - Groq API key almak için [groq.com](https://groq.com) adresini ziyaret edin

5. **Çalıştırın**
   - Emülatör veya fiziksel cihaz bağlayın
   - Run butonu (▶️) ile uygulamayı başlatın

## 🔧 API Konfigürasyonu

Uygulama Groq API kullanmaktadır. Güvenlik nedeniyle API key kod içinde bulunmamaktadır:

```kotlin
// ApiConstants.kt
object ApiConstants {
    const val BASE_URL = "https://api.groq.com/"
    const val API_KEY = "YOUR_GROQ_API_KEY_HERE" // Buraya kendi key'inizi ekleyin
}
```

> ⚠️ **Güvenlik Notu**: Production uygulamasında API key'leri environment variable veya güvenli bir config dosyasında saklanmalıdır.

## 📱 Ekran Akışı

1. **Splash Screen** - GiftGenie logosu ve yükleme
2. **Welcome Screen** - Hoş geldin mesajı ve "Hemen Başla" butonu
3. **Questions Screen** - 15 sorunun sırayla sorulduğu ekran
4. **Loading Screen** - AI analizi ve hediye önerileri hazırlanıyor
5. **Results Screen** - 3 kişiselleştirilmiş hediye önerisi

## 🎨 UI/UX Özellikleri

- **Gradient Background**: Mor tonlarında modern gradient
- **Material Design 3**: En güncel tasarım dilı
- **Responsive Cards**: Çeşitli içerik tipleri için esnek kartlar
- **Smooth Animations**: Geçişler ve yükleme animasyonları
- **Intuitive Navigation**: Kolay ileri/geri navigasyon
- **Progress Tracking**: Soru ilerlemesi göstergesi

## 📂 Önemli Dosyalar

- `MainActivity.kt` - Ana activity ve uygulama başlangıcı
- `GiftViewModel.kt` - Ana uygulama state yönetimi
- `GiftRepository.kt` - API çağrıları ve business logic
- `QuestionsData.kt` - Tüm soru ve seçeneklerin tanımı
- `Navigation.kt` - Ekranlar arası navigasyon

## 🔧 Özelleştirme

### Yeni Soru Ekleme
`QuestionsData.kt` dosyasında yeni sorular ekleyebilirsiniz:

```kotlin
Question(
    id = 16,
    text = "Yeni soru metni?",
    options = listOf("Seçenek 1", "Seçenek 2"),
    type = QuestionType.SINGLE_CHOICE
)
```

### Tema Değişiklikleri
`ui/theme/` klasöründeki dosyalardan renk ve stil özelleştirmeleri yapabilirsiniz.

### API Modeli Güncelleme
`ApiModels.kt` dosyasından request/response yapısını güncelleyebilirsiniz.

## 🐛 Bilinen Sorunlar ve Çözümler

- **Network Error**: İnternet bağlantısını kontrol edin
- **API Limit**: Groq API limitleri aşılırsa bekleme süresi gerekebilir
- **JSON Parsing Error**: API yanıtı beklenmeyen formatta gelirse fallback metin parsing devreye girer

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add some amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📧 İletişim

Herhangi bir soru veya öneriniz için issue açabilirsiniz.

---

**GiftGenie** ile mükemmel hediyeyi bulun! 🎁✨ 