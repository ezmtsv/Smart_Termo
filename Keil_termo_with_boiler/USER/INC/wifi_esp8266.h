#ifndef __WIFI_ESP8266_H
#define __WIFI_ESP8266_H

#include "usart_stm.h"
#include <string.h>
#include "stdbool.h"

typedef enum {
	com_status,
	com_get_data,
	com_get_busy,
	com_get_connect,
	com_get_closed,
	com_get_link_not_valid,
	com_get_error,
	com_no
} com_ESP8266;

typedef enum {
	acc_mail = 3,
	acc_yandex = 4,
	acc_google = 5,
  no_mailAcc = 0
} Acc_;

/*
typedef struct{
	unsigned char IP_adr [4];							// IP адрес подключаемой сети
	unsigned char SSID_wifi [16];					// имя сети не должно превышать 16 символов, и символы не должны содержать 255(символ 'я') 
	unsigned char pass_wifi [8]; 					//	пароль для подключения к сети
	unsigned char status[8];							//	флаг ответа на запрос статуса, начало ответа " STATUS:"
	unsigned char flag_recieved[5];				// флаг приема данных "+IPD,"
	uint16_t	potrserv;
	unsigned char flag_busy[8];						// флаг занятости сервера " busy p."
	unsigned char flag_connected[8];			// флаг подключения к сети ",CONNECT"
	unsigned char flag_closed[7];					// флаг отключения от сети ",CLOSED"
	unsigned char command_sendwifi[11];		// команда на отправку клиенту данных "AT+CIPSEND="
	unsigned char flag_sendOK[8];					// флаг успешной отправки данных клиенту "\n"+"SEND OK"
	unsigned char MAC_adr [6];						// MAC адрес устройства
	unsigned char empty[17];					
	unsigned char lost_con[17];						// флаг, возникает при обрыве соединения
}param_ESP8266;
*///////////при объевлении структуры  в виде, как выше, а потом объявлении переменной структуры как - param_ESP8266* init_param, возникают глюки, зависает  МК
struct param_ESP8266 {
	unsigned char IP_adr [12];							// IP адрес в сети
	unsigned char SSID_wifi [16];					// имя сети не должно превышать 16 символов, и символы не должны содержать 255(символ 'я') 
	unsigned char pass_wifi [8]; 					//	пароль для подключения к сети
	unsigned char status[8];							//	флаг ответа на запрос статуса, начало ответа " STATUS:"
	unsigned char flag_recieved[5];				// флаг приема данных "+IPD,"
	uint16_t	potrserv;
	unsigned char flag_busy[8];						// флаг занятости сервера " busy p."
	unsigned char flag_connected[8];			// флаг подключения к сети ",CONNECT"
	unsigned char flag_closed[7];					// флаг отключения от сети ",CLOSED"
	unsigned char command_sendwifi[11];		// команда на отправку клиенту данных "AT+CIPSEND="
	unsigned char flag_sendOK[8];					// флаг успешной отправки данных клиенту "\n"+"SEND OK"
	unsigned char MAC_adr [6];						// MAC адрес устройства
	unsigned char empty[17];					
	unsigned char lost_con[17];						// флаг, возникает при обрыве соединения
	unsigned char wifi_gotIP[11];					// флаг получения IP адреса в сети
	unsigned char req_IP[6];							// флаг получения ответа на запрос IP ":STAIP"  - в режиме станции, и ":APIP" в режиме точки доступа
	unsigned char req_MAC[6];							// флаг получения ответа на запрос MAC "STAMAC" - в режиме станции, и  ":APMAC" в режиме точки доступа
};
#define get_strBUF_USB(x)   getstr_BUF_USB((uint8_t*)(x))
//#define send_strUSB(x)   send_str_USB((uint8_t*)(x))

void getstr_BUF_USB (uint8_t *s);
void send_str_USB (char *s, uint8_t count);
bool wait_OK(void);
bool check_exist_CON (uint8_t num);
void recieved_word (unsigned char RXc, uint16_t count_in);
void init_paramESP8266(void);
void init_WIFI_server(void);
void sendUSB_getCOM_data (uint16_t count_byte, uint16_t start);
uint16_t size_get_bytes_from_com (uint16_t start);
void send_data_clientWIFI (uint8_t buf[], uint16_t len);
void ready_send_dataWIFI (uint8_t con, uint8_t len, uint8_t buf_send[]);
//void create_AP_termo(void);
//void set_newNET(char* net, char* pass, uint32_t len);
void set_newNET (uint8_t dim[]);
void send_MAIL (char* txt_mail_str, char* sub_mail, bool from_dim_txt);
uint8_t func_acs2 (double k);
void init_const_txt(void);
void ready_rep(void);
#endif
/**
для прерывания отправки данных в утилите "F:\IRON\STM32\STM32F072\UART_sample\COMP_test\test-USB" надо отправить любую команду
в ком порт, для возобновления отправить любые данные по кнопке "write"
*/
/*
Протокол обмена между девайсами в режиме работы модуля сервер
Первые 64 байта - обмен в символьном режиме, следующие до 192 включительно заполняем числами
data_OUT_COM[0] - data_OUT_COM[3]  - преамбула "EZAP"

data_OUT_COM[4] - data_OUT_COM[16] - 12 байт под МАС адрес устройства

data_OUT_COM[17] - статус
	bit 0				- 			1 - сервер, 0 - клиент
	bit 1-bit4 	-				идентификация устройства, 1-термостат, 2-розетка, 3-освещение, 4 - оператор
	bit 5   -           1 - команда, 0 - запрос данных
	

data_OUT_COM[18] - идентификация мощности подключенного устройства
bit 0-bit2	- 			мощность, 0 - до 100вт, 1 - от 100 до 200Вт, 2 - от 200 до 500Вт, 3 - от 500 до 1000Вт, 4 - от 1000 до 2000Вт, 5 - от 2кВт	до 4000Вт, 6 - от 4кВт	

data_OUT_COM[18]	-	для сервера
	bit 0-bit4			- кол-во подключенных клиентов
	bit 5-bit7			- 

data_OUT_COM[19] - data_OUT_COM[30] - IP адрес полученный устройством в домашней сети 12 символов
data_OUT_COM[31] - data_OUT_COM[34] - для сервера, порт открытый для приема 4 символа
data_OUT_COM[35] - data_OUT_COM[50] - имя сети 16 символов
data_OUT_COM[51] - 										длина имени сети (кол-во символов в имени)
data_OUT_COM[52] - data_OUT_COM[59] - ключь сети
data_OUT_COM[60] - data_OUT_COM[63] - резерв

data_OUT_COM[64] - data_OUT_COM[128] - данные по работе термостата
При старте девайса МК считывает из памяти настройки, при отсутствии сохраненного MAC, IP, password
выставляется соответствующий флаг и выдается сообщение в USB порт.
При подключении клиента, он передает команду серверу на сопоставлении своего МАС адреса и номера подключения
//6A:C6:3A:A7:8F:ED// mac adr SoftAP (точка доступа)
//68:C6:3A:A7:8F:ED// mac adr WiFi клиент
?? server MAC 44:c3:46:b1:ed:62??
server MAC 68:c6:3a:a7:8f:ed

link is not valid
VIBE K5 Note//EvanAmaliya7

AT+CWSAP_CUR	Создать SoftAP (точку доступа) для текущего сеанса	wifi	
AT+CWSAP_CUR= <идентификатор сети>,<пароль>,<канал>, <тип шифрования>	
AT+CWSAP_CUR? возвращает текущие параметры точки доступа	
Команда доступна только когда модуль находится в режиме SoftAP (точка доступа). Требуется AT+RST. 
SSID и пароль указываются в двойных кавычках. Пароль не более 64 символов. 
Типы шифрования: 0:Open, 2:WPA_PSK, 3:WPA2_PSK, 4:WPA_WPA2_PSK (Шифрование WEP недоступно в этой версии)
Пример: AT+CWSAP_CUR="ESP8266","1234567890",5,3
Если хотим  сделать сеть открытой без ведения пароля, то в поле пароль указываем любую комбинацию символов, главное тип шифрования выставляем в 0. 
Пример: AT+CWSAP_CUR="ESP8266","1234567890",5,0

AT+CWSAP_DEF	Команда полностью аналогична AT+CWSAP_CUR	wifi			Параметры команды сохраняются во флеш память и загружаются при следующем старте модуля.

В режиме точки дотупа у модуля всегда IP 192.168.4.1
AT+CIFSR +CIFSR:APIP,"192.168.4.1"
+CIFSR:APMAC,"6a:c6
:3a:a7:8f:ed"

При записи параметров начальной инициализации (имя сети, пароль, майл и т.д.), модуль нужно перевести в режим точки доступа, длительным удержанием кнопки
(при первой настройке он уже в этом режиме, при следующих - переводим его в этот режим - команда AT+CWMODE=2)
При приходе от приложения команды set_link, сохраняем в памяти имя сети и пароль и инициализируем его в новой сети (AT+CWJAP="ez_dir300","1969EvAn"), получаем IP адрес,
опять запускаем сеть Termostat_EZAP(команда AT+CWMODE=2), отправляем приложению подтверждение команды и IP адрес модуля 


*/

