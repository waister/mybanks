# MyBanks Android App

O **MyBanks** é uma aplicação Android moderna desenvolvida para organizar, gerenciar, copiar e compartilhar contas bancárias, números de agência/conta e chaves PIX, com suporte a backup em nuvem, atualização in-app e monetização via anúncios Google AdMob.

---

## 🚀 Tecnologias

- **Linguagem:** Kotlin (Java 21)
- **UI:** Jetpack Compose (BOM), Material 3, Navigation Compose (Single-Activity Architecture)
- **Arquitetura:** MVVM com Unidirectional Data Flow (UDF) usando `StateFlow` e `SharedFlow`
- **Injeção de Dependências:** Koin
- **Rede:** Retrofit + OkHttp + Gson com `BaseParamsInterceptor`
- **Banco de Dados Local:** Room (KSP, Flow e Coroutines)
- **Preferências:** SharedPreferences encapsulado via `PreferencesRepository`
- **Push Notifications & Analytics:** Firebase Cloud Messaging (FCM), Firebase Analytics, Firebase Crashlytics
- **Monetização:** Google AdMob (Adaptive Banner, Interstitial, Rewarded e App Open Ads)
- **Qualidade de Código:** Gradle Kotlin DSL (`build.gradle.kts`), Version Catalogs (`libs.versions.toml`), ktlint e Kover
- **Testes:** JUnit4, MockK, Turbine, Robolectric e Coroutines Test

---

## 🏗️ Estrutura do Projeto

O projeto é estruturado por camadas e features:

```
app/src/main/java/com/duduapps/mybanks/
├── activity/                  # MainActivity (Single-Activity Compose)
├── application/               # CustomApplication (inicialização do Koin, Ads)
├── data/
│   ├── local/                 # Room Database, DAOs e Entities
│   └── repository/            # Repositórios (Account, Bank, Auth, Feedback, Preferences)
├── di/                        # Módulos do Koin (App, Local, Network, Repository, ViewModel)
├── features/                  # Features da aplicação
│   ├── splash/                # SplashScreen e inicialização de configurações
│   ├── main/                  # MainScreen (listagem de contas, busca, cópia/compartilhamento)
│   ├── account/
│   │   ├── detail/            # AccountDetailScreen (detalhes da conta, cópia de campos)
│   │   └── form/              # AccountFormScreen (cadastro e edição de contas)
│   ├── auth/                  # LoginScreen (autenticação por e-mail com código)
│   ├── removeads/             # RemoveAdsScreen (recompensa por vídeo assistido)
│   └── feedback/              # FeedbackScreen (envio de mensagens para suporte)
├── models/                    # Modelos de domínio e DTOs de API
├── navigation/                # AppNavHost e Rotas da aplicação
├── network/                   # ApiService (Retrofit) e Interceptor
├── service/                   # MyFirebaseMessagingService
├── ui/
│   ├── components/            # Componentes reutilizáveis (TopBar, Banner, Dialogs)
│   └── theme/                 # Cores, Tipografia e Tema Material 3
└── utils/                     # Gerenciadores de anúncios (AppOpen, Interstitial)
```

---

## 🧪 Testes e Qualidade

O projeto conta com ampla cobertura de testes unitários para Repositórios e ViewModels.

```bash
# Executar todos os testes unitários
./gradlew testDebugUnitTest

# Verificar estilo de código com ktlint
./gradlew ktlintCheck

# Corrigir automaticamente formatação com ktlint
./gradlew ktlintFormat

# Compilar Kotlin
./gradlew compileDebugKotlin

# Gerar APK de debug
./gradlew assembleDebug
```

---

## 📦 Build e Versionamento

O gerenciamento de dependências e plugins é centralizado no Version Catalog:
- `gradle/libs.versions.toml`
- `build.gradle.kts` (raiz)
- `app/build.gradle.kts` (módulo da aplicação)
- `settings.gradle.kts`

---
Desenvolvido com foco em modernização, estabilidade e arquitetura limpa.
