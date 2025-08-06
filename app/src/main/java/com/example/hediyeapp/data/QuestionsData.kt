package com.example.hediyeapp.data

object QuestionsData {
    val questions = listOf(
        Question(
            id = 1,
            text = "Bu kişiyle ilişkiniz nedir?",
            options = listOf(
                "Ailemden biri",
                "Sevgilim / Eşim",
                "Arkadaşım",
                "İş arkadaşım",
                "Diğer"
            )
        ),
        Question(
            id = 2,
            text = "Cinsiyeti nedir?",
            options = listOf(
                "Kadın",
                "Erkek",
                "Belirtmek istemiyorum"
            )
        ),
        Question(
            id = 3,
            text = "Yaşı kaç?",
            options = listOf(
                "0-12",
                "13-17",
                "18-25",
                "26-35",
                "36-50",
                "50+"
            )
        ),
        Question(
            id = 4,
            text = "Mesleği nedir?",
            options = listOf(
                "Öğrenci",
                "Akademisyen",
                "Yazılımcı / Mühendis",
                "Sağlık çalışanı",
                "Sanatçı",
                "Serbest çalışan",
                "Diğer"
            )
        ),
        Question(
            id = 5,
            text = "Boş zaman aktiviteleri? (çoktan seçmeli)",
            options = listOf(
                "Kitap okumak",
                "Film / dizi izlemek",
                "Spor yapmak",
                "Oyun oynamak",
                "Seyahat etmek",
                "Müzik dinlemek",
                "El işi / sanat",
                "Yemek yapmak"
            ),
            type = QuestionType.MULTIPLE_CHOICE
        ),
        Question(
            id = 6,
            text = "Teknolojiye ilgisi var mı?",
            options = listOf(
                "Çok ilgili",
                "Orta seviye",
                "Az"
            )
        ),
        Question(
            id = 7,
            text = "Sanata ilgisi var mı?",
            options = listOf(
                "Evet",
                "Hayır",
                "Bilmiyorum"
            )
        ),
        Question(
            id = 8,
            text = "Kişilik tipi nedir?",
            options = listOf(
                "Enerjik",
                "Romantik",
                "Duygusal",
                "Sosyal",
                "Sessiz / sakin",
                "Planlı / titiz"
            )
        ),
        Question(
            id = 9,
            text = "Modaya ilgisi var mı?",
            options = listOf(
                "Evet",
                "Hayır",
                "Bilmiyorum"
            )
        ),
        Question(
            id = 10,
            text = "Tarzı nasıldır?",
            options = listOf(
                "Minimalist",
                "Gösterişli",
                "Karışık"
            )
        ),
        Question(
            id = 11,
            text = "Daha önce ne hediye aldınız?",
            type = QuestionType.TEXT_INPUT
        ),
        Question(
            id = 12,
            text = "Hediye amacı nedir?",
            options = listOf(
                "Doğum günü",
                "Sevgililer günü",
                "Yeni yıl",
                "Sürpriz",
                "Mezuniyet",
                "Yeni iş / taşınma"
            )
        ),
        Question(
            id = 13,
            text = "Teslimat aciliyeti:",
            options = listOf(
                "Acil (1-3 gün)",
                "Farketmez"
            )
        ),
        Question(
            id = 14,
            text = "Hediye türü tercihi:",
            options = listOf(
                "Fiziksel",
                "Dijital",
                "Farketmez"
            )
        ),
        Question(
            id = 15,
            text = "Bütçeniz nedir?",
            options = listOf(
                "0-100 TL",
                "100-250 TL",
                "250-500 TL",
                "500-1000 TL",
                "1000 TL+"
            )
        )
    )
} 