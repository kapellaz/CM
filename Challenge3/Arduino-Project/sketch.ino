#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <WiFi.h>
#include <PubSubClient.h>

// Configurações do LCD
LiquidCrystal_I2C lcd(0x27, 16, 2); // Endereço padrão do LCD
// Definindo pinos do LED RGB
const int redPin = 12;    // Pino do LED vermelho
const int greenPin = 13;  // Pino do LED verde
const int bluePin = 14;   // Pino do LED azul

// Configurações do Wi-Fi
const char *ssid = "Wokwi-GUEST"; // Rede Wi-Fi
const char *password = "";        // Senha do Wi-Fi (se aplicável)

// Configurações do MQTT
const char *mqttServer = "test.mosquitto.org"; // Servidor MQTT
const int mqttPort = 1883;    
const char *mqttTopic = "chat/arduinooooo";       // Tópico de mensagens

WiFiClient espClient;
PubSubClient client(espClient);
// Função para definir a cor do LED
void setLedColor(int red, int green, int blue) {
  analogWrite(redPin, red);
  analogWrite(greenPin, green);
  analogWrite(bluePin, blue);
}
// Função chamada ao receber mensagens MQTT
void callback(char *topic, byte *payload, unsigned int length) {
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  // Exibe a mensagem recebida no LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(message);

  
 setLedColor(255, 255, 255); // Começa com a cor branca no LED (todos os LEDs acesos)
 
  
}




void setup() {
  // Inicializa comunicação serial
  Serial.begin(115200);

  // Inicializa LCD
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Connecting...");
   // Inicializa os pinos do LED RGB
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin, OUTPUT);
  
  // Conecta ao Wi-Fi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  
  // Configura MQTT
  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);

  // Conecta ao MQTT
  while (!client.connected()) {
    Serial.println("Connecting to MQTT...");
    if (client.connect("ArduinoClient")) {
      Serial.println("Connected to MQTT  - SUBS");
      client.subscribe(mqttTopic); // Subscreve ao tópico para mensagens
    } else {
      Serial.print("Failed with state ");
      Serial.println(client.state());
      delay(2000);
    }
  }

  // Mensagem inicial no LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Waiting for");
  lcd.setCursor(0, 1);
  lcd.print("messages...");
}

void reconnect() {
  while (!client.connected()) {
    if (client.connect("ArduinoClient")) {
      client.subscribe("chat/arduinooooo");  // Inscreva-se no tópico quando reconectar
    } else {
      delay(5000);  // Espera 5 segundos antes de tentar novamente
    }
  }
}

void loop() {
  if (!client.connected()) {
    reconnect();  // Se desconectar, tenta reconectar
  }
 
  client.loop();  // Processa mensagens recebidas
}

