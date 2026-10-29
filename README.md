# Android Auto Central Multimídia 7" (1920x1080p) - Launcher Nativo

Aplicativo Launcher Automotivo nativo desenvolvido especificamente para **Centrais Multimídia Android de 7 polegadas com resolução Full HD (1920x1080p)**. 

O app recria a experiência e layout do **Android Auto (Coolwalk UI)** com tela dividida (Split-Screen Dashboard), tocador de mídia integrado ao **Spotify**, mapa dinâmico integrado ao **Google Maps / Waze** e gaveta de aplicativos nativos da central, **sem necessidade de conectar qualquer smartphone**.

---

## 🚀 Principais Funcionalidades

1. **Launcher Nativo com Inicialização Automática no Carro:**
   - Registrado como tela inicial padrão do sistema (`android.intent.category.HOME` e `DEFAULT`).
   - Inicializa automaticamente ao ligar o veículo (`BOOT_COMPLETED` e `QUICKBOOT_POWERON`).
   - Modo Fullscreen Imersivo (bloqueia barras indesejadas e mantém a tela sempre ativa com foco na condução).

2. **Dashboard Android Auto Coolwalk (Split-Screen 1080p):**
   - **Lado Esquerdo (60% da tela):**
     - **Mapa Integrado Interativo:** Renderização fluida com tema noturno automotivo (OSMDroid), rastreamento em tempo real do veículo e bússola direcional.
     - **Velocímetro Digital HUD:** Leitura de velocidade em tempo real (`km/h`) diretamente via GPS de alta precisão.
     - **Barra de Destino Rápida ("Para onde vamos?"):** Permite digitar um endereço ou tocar em categorias rápidas (Postos de gasolina, Restaurantes, etc.).
     - **Integração com Google Maps e Waze:** Botões dedicados de 1 toque para abrir instantaneamente o trajeto no Google Maps ou no Waze instalados na central.
     - **Botão Recentralizar GPS:** Mantém o veículo sempre no centro da tela.
   - **Lado Direito Superior (Card de Mídia):**
     - **Tocador integrado ao Spotify:** Controle total do Spotify (Play, Pause, Próxima faixa, Faixa anterior, barra de progresso).
     - **Capa do Álbum e Metadados:** Exibe capa em alta resolução, nome da música, artista e badge oficial do Spotify.
     - **Universal MediaSession:** Além do Spotify, também reconhece e controla outros players (Rádio FM/AM da central, YouTube Music, Deezer, Pen drive).
     - **Estado Vazio Inteligente:** Caso o Spotify esteja fechado, exibe um botão de 1 toque para inicializá-lo diretamente.
   - **Lado Direito Inferior (Card Contextual):**
     - Data completa em português, status do sistema e atalhos rápidos ("Ir para Casa" e "Waze Rota").

3. **Barra de Navegação / Dock (Coolwalk Taskbar):**
   - **Botão Grade de Aplicativos (9 pontos):** Alterna instantaneamente entre o Dashboard e a Gaveta de Aplicativos.
   - **Atalhos Rápidos Centrais:** Acesso ao Mapa, Spotify e Configurações da Central.
   - **Assistente de Voz / Google Assistant:** Botão dedicado com o ícone das 4 cores do Google para comandos de voz hands-free.
   - **Controles do Veículo:** Atalho de Volume e indicador de sinal Wi-Fi.
   - **Relógio Digital Automotivo:** Relógio de alta visibilidade sempre atualizado.

4. **Gaveta de Aplicativos Nativos da Central:**
   - Lê todos os aplicativos instalados na multimídia (`PackageManager`).
   - Grade adaptada para telas de 7" em 1080p (ícones grandes e fáceis de tocar durante paradas).
   - Campo de busca instantâneo com filtro em tempo real.
   - Abre qualquer app nativo (Rádio, Bluetooth, Câmera de ré/DVR, YouTube, Play Store, etc.).

---

## 🛠️ Tecnologias e Arquitetura

- **Linguagem:** Kotlin 1.9+
- **Target SDK:** Android 14 (API 34) | **Min SDK:** Android 7.0 (API 24) – compatível com praticamente qualquer central multimídia do mercado (Allwinner, TS10, UIS7862, Rockchip, MediaTek).
- **Interface:** Material Design 3 Dark Automotive Theme, ConstraintLayout e ViewBinding.
- **Mapas:** OSMDroid (OpenStreetMap Android) com tiles Dark Matter em cache local – **funciona sem precisar de chave de API paga**.
- **Mídia:** Android `MediaSessionManager` + `NotificationListenerService` para captura e controle de qualquer áudio em reprodução.
- **Coroutines & Flow:** Reatividade rápida para velocímetro e metadados de áudio.

---

## 📁 Estrutura do Projeto

```
MeuApp/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/carlauncher/auto/
│       │   ├── AutoCarApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── model/
│       │   │   ├── AppItem.kt
│       │   │   └── MediaTrack.kt
│       │   ├── service/
│       │   │   ├── BootReceiver.kt
│       │   │   ├── CarMediaListenerService.kt
│       │   │   └── MediaControllerManager.kt
│       │   ├── ui/
│       │   │   ├── apps/
│       │   │   │   ├── AppsAdapter.kt
│       │   │   │   └── AppsFragment.kt
│       │   │   ├── dashboard/
│       │   │   │   └── DashboardFragment.kt
│       │   │   ├── maps/
│       │   │   │   ├── DestinationSearchDialog.kt
│       │   │   │   └── MapWidgetFragment.kt
│       │   │   └── media/
│       │   │       └── MediaWidgetFragment.kt
│       │   └── utils/
│       │       ├── AppLauncherHelper.kt
│       │       └── LocationSpeedManager.kt
│       └── res/
│           ├── drawable/          # Ícones vetoriais Coolwalk, Spotify, Maps, Waze
│           ├── layout/            # Layouts XML otimizados para 1920x1080
│           ├── values/            # Cores, dimensões, strings e temas
│           └── values-night/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

---

## 📲 Como Gerar o APK e Instalar na Central Multimídia

### Opção 1: Compilar usando Android Studio
1. Abra o **Android Studio**.
2. Clique em **File > Open** e selecione a pasta deste projeto (`d:\Downloads\MeuApp`).
3. Aguarde o Gradle sincronizar as dependências.
4. Para gerar o instalador APK:
   - Vá no menu superior em **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - O APK gerado estará em: `app/build/outputs/apk/debug/app-debug.apk`.

### Opção 2: Compilar via Linha de Comando (Terminal)
No terminal dentro da pasta do projeto, execute:
```bash
./gradlew assembleDebug
```
Para gerar a versão otimizada de produção:
```bash
./gradlew assembleRelease
```

---

## 🚗 Instalação e Configuração na Central

1. **Copiar o APK:**
   - Coloque o arquivo `app-debug.apk` em um Pen Drive USB.
   - Conecte o Pen Drive na entrada USB da Central Multimídia do carro.
   - Abra o aplicativo "Gerenciador de Arquivos" da central e instale o APK.

2. **Definir como Tela Inicial Padrão (Launcher):**
   - Na central multimídia, vá em **Configurações do Android > Aplicativos > Aplicativos Padrão > App de Início (Home App)**.
   - Selecione **Android Auto Central** como o app de início padrão.
   - A partir desse momento, toda vez que ligar o carro ou pressionar o botão "Home" da central/volante, o app será aberto automaticamente.

3. **Ativar Permissão de Controle do Spotify:**
   - Ao abrir o app pela primeira vez, surgirá um aviso solicitando permissão de **Acesso às Notificações**.
   - Toque em **Configurar** e ative o botão para o **Serviço de Controle de Mídia Automotivo**. Essa permissão é o recurso nativo do Android que permite ao launcher controlar músicas, ler a capa do álbum e pausar/avançar faixas no Spotify sem precisar de chaves de API externas.

4. **Permissão de GPS:**
   - Permita o acesso à **Localização Precisa** para que o mapa e o velocímetro funcionem em tempo real.
