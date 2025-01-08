// // Defina o pino de leitura analógica
// const int potPin = 34;  // Pino ADC (Entrada Analógica)

// // Variável para armazenar o valor lido do potenciômetro
// int potValue = 0;

// void setup() {
//   // Inicializa a comunicação serial para exibir os resultados no monitor serial
//   Serial.begin(115200);

//   // Configura o pino 34 como entrada (opcional, pois o ADC já assume como entrada)
//   pinMode(potPin, INPUT);
// }

// void loop() {
//   // Lê o valor analógico do potenciômetro (0 a 4095 para ESP32)
//   potValue = analogRead(potPin);

//   // Exibe o valor lido no Monitor Serial
//   Serial.print("Valor lido do potenciômetro: ");
//   Serial.println(potValue);

//   // Aguarde 200 ms antes de ler novamente
//   delay(200);
// }
