#include <PubSubClient.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <WiFi.h>

// Define o LCD
LiquidCrystal_I2C LCD = LiquidCrystal_I2C(0x27, 16, 2);

// Configurações de Wi-Fi
const char *SSID = "Wokwi-GUEST";
const char *PASSWORD = "";

// Configurações do Broker MQTT
const char *BROKER_MQTT = "test.mosquitto.org";
int BROKER_PORT = 1883;

// Tópicos MQTT (serão configurados dinamicamente)
String username = "bruno"; // Altere para testar com outros usuários
String TOPIC_SEND = "cmGymTrackerSend/+" ;
String TOPIC_RECEIVE = "cmGymTrackerReceive/";

// Variáveis Globais
WiFiClient espClient;
PubSubClient MQTT(espClient);

// Funções de Inicialização e Reconexão
void startWifi(void);
void initMQTT(void);
void reconnectMQTT(void);
void reconnectWiFi(void);
void checkWiFIAndMQTT(void);

// Callback para mensagens recebidas
void callbackMQTT(char *topic, byte *payload, unsigned int length) {
  String msg;

  // Converte o payload para String
  for (int i = 0; i < length; i++) {
    msg += (char)payload[i];
  }

  Serial.printf("Mensagem recebida no tópico: %s\n", topic);
  Serial.printf("Mensagem: %s\n", msg.c_str());
   String user = String(topic).substring(17); // Ignora a parte "cmGymTrackerSend/" do tópico
  String responseTopic = TOPIC_RECEIVE + user;  // Cria o tópico de resposta com o mesmo usuário


if (String(topic).startsWith("cmGymTrackerSend/")) {
  
    Serial.println("Pedido recebido, gerando valores...");

    // Gera os valores aleatórios para pulsação, oxigenação e batimentos cardíacos
    int pulseValue = random(60, 150); // Pulsação entre 60 e 100 bpm
    int oxygenValue = random(90, 100); // Oxigenação entre 90% e 100%
 
    // Cria a mensagem concatenando os três valores
    String message = String(pulseValue) + "," + String(oxygenValue) ;
      MQTT.subscribe(responseTopic.c_str());
    // Publica o valor no tópico de resposta
    MQTT.publish(responseTopic.c_str(), message.c_str());
    Serial.printf("Mensagem publicada em %s: %s\n", TOPIC_RECEIVE.c_str(), message.c_str());

    // Atualiza o LCD
    LCD.clear();
    LCD.setCursor(0, 0);
    LCD.print("Pedido recebido");
    LCD.setCursor(0, 1);
    LCD.print("Valor: " + String(pulseValue) + " " + String(oxygenValue));
}
}

// Configuração inicial do Wi-Fi
void startWifi(void) {
  reconnectWiFi();
}

// Inicializa o MQTT
void initMQTT(void) {
  MQTT.setServer(BROKER_MQTT, BROKER_PORT);
  MQTT.setCallback(callbackMQTT);
}

// Reconecta ao MQTT se desconectado
void reconnectMQTT(void) {
  while (!MQTT.connected()) {
    Serial.println("Conectando ao Broker MQTT...");
    if (MQTT.connect("ArduinoClient")) {
      Serial.println("Conectado ao Broker!");

      // Inscreve-se no tópico de pedidos
      MQTT.subscribe(TOPIC_SEND.c_str());
      Serial.printf("Inscrito no tópico: %s\n", TOPIC_SEND.c_str());

      // Atualiza o LCD
      LCD.clear();
      LCD.setCursor(0, 0);
      LCD.print("MQTT conectado");
      LCD.setCursor(0, 1);
      LCD.print("Aguardando msgs...");
    } else {
      Serial.println("Falha ao conectar ao Broker. Tentando novamente...");
      delay(2000);
    }
  }
}

// Reconecta ao Wi-Fi se desconectado
void reconnectWiFi(void) {
  if (WiFi.status() == WL_CONNECTED) return;

  WiFi.begin(SSID, PASSWORD);
  Serial.print("Conectando ao Wi-Fi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }

  Serial.println("\nWi-Fi conectado!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  LCD.clear();
  LCD.setCursor(0, 0);
  LCD.print("Wi-Fi conectado");
  LCD.setCursor(0, 1);
  LCD.print(WiFi.localIP());
}

// Verifica o estado do Wi-Fi e MQTT
void checkWiFIAndMQTT(void) {
  if (!MQTT.connected()) reconnectMQTT();
  if (WiFi.status() != WL_CONNECTED) reconnectWiFi();
}

// Configuração Inicial
void setup() {
  Serial.begin(115200);

  // Inicializa o LCD
  LCD.init();
  LCD.backlight();
  LCD.setCursor(0, 0);
  LCD.print("Iniciando...");

  // Configura Wi-Fi e MQTT
  startWifi();
  initMQTT();

  // Semente para valores aleatórios
  randomSeed(analogRead(0));
}

// Loop Principal
void loop() {
  checkWiFIAndMQTT();
  MQTT.loop(); // Mantém a conexão com o Broker
}
