#include "wifi_esp8266.h"

/*
unsigned char IP_adr [] = {192, 168, 1, 1};
unsigned char SSID_wifi [] = {'E', 't', 'a', 'n'};
unsigned char pass_wifi [] = {'4','h','-','a','g','K','7','&'};
*/

////////////////////////////////////////////////////////
struct	param_ESP8266 init_param;
////////////////////////////////////////////
////////////////////////////////////////////
com_ESP8266 flag_getESP8266;
uint8_t len_nameSSID;
uint16_t count_start_dataWIFI, length_dataWIFI;
uint8_t	count_status_word = 0;
uint8_t	count_getdata_word = 0;
uint8_t	count_getbusy_word = 0;
uint8_t	count_getconnect_word = 0;
uint8_t	count_getclosed_word = 0;
uint8_t count_get_sendOK = 0;
uint8_t count_gotIP = 0;
uint8_t count_req_IP = 0;
uint8_t count_get_link_not_valid = 0;
uint8_t number_connect_WIFI;
uint8_t all_number_connect_WIFI = 0;

uint8_t mail_from[40];
uint8_t mail_from_len;
uint8_t mail_from64[32];
uint8_t mail_from_len64;
uint8_t mail_pass64[28];
uint8_t mail_pass_len64;
uint8_t mail_to[40];
uint8_t mail_to_len;
uint8_t mail_name_port[3];
uint8_t mail_name_serv[30];
uint8_t mail_name_serv_len;

uint16_t count_modul_disconnect = 0;
uint8_t dim_data_fromESP[260];
uint8_t acs2_symb[8];
Acc_ account_mail;

unsigned char RXc_last;

bool flag_OK_comWIFI = false;
bool flag_initESP8266 = true;
bool flag_start_get_dataCOM = false;
bool end_timeout_getdata = false;
bool flag_start_sendWIFI = true;
bool flag_wait_sendWIFI = false;
bool flag_end_sendWIFI = false;
bool flag_make_end = true;
bool flag_gotIP = false;
bool com_sendOK = false;
bool modul_ESP_connect = false;
bool start_initESP = false;
bool exist_connect[10] = {false, false, false, false, false, false, false, false, false, false}; // массив для хранения статуса подключений cliet WIFI
bool debug_Transmit_USB = false;
bool single_connection = false;
bool flag_req_IP = false;	
bool flag_req_IP_end = false;	
/////////////////////////////////////
extern uint8_t data_OUT_COM[192];
extern uint16_t len_packet_COM;
extern uint32_t	countIN_com;
extern uint8_t Send_Buffer[64];
extern unsigned char in_comPORT[300];
extern unsigned char acs2_data[20];
bool execute_new_net = false; 														// флаг для запрета инициализации модуля во время установки новых параметров сети
uint8_t ip_adr_getESP[20];

uint8_t count_word_ip = 0; bool flag_start_count_wordIP = false;

double rep_day[24][6];
uint8_t txt_pole[6][34];
uint8_t txt_pole_len[6];
uint8_t txt_mail[3000];

const uint8_t str_error[5] = {'E','R','R','O','R'};
uint8_t count_bait_err = 0;
bool flag_er_command = false;
////////////////////////////////////////////////////////
uint8_t receive_word[12];
uint16_t len_word;
uint16_t count_len_word;
bool wait_word = false;
bool wait_word_got = false;

uint8_t strword[36] = {'T','i','m','e','o','u','t',' ','w','a','i','t',' ','W','o','r','d',' '};

////////////////////////////////////////////////////////
void DelaymS (__IO uint32_t nCount);
void data_answer(void);

////////////////////////////////////////////////////////////////////////////
void send_CIPSEND_mail (uint16_t len, uint8_t buf_send[]);	
/*
	отправка модулю контента для эл. письма с ожиданием получения от почтового сервера подтверждения полученных данных
	word - слово, которое мыдолжны получить от почтового сервера в качестве подтверждения (слово должно быть не более 12 символов)
	time - время, в течении которого мы ждем ответа от почтового сервера
	len_ - длина массива символов передаваемых серверу
	buf_[] - массив символов отправляемых серверу
	:220 - ответ на запрос подключения к серверу
	:250 - ответ на запрос приветствия "Helo 192.168.1.46"
	:334 - ответ на запрос "AUTH LOGIN"
	:334 - ответ при передаче адреса отправителя в формате BASE64
	:235 - ответ при передаче пароля отправителя в формате BASE64 в случае успешной авторизации
	:250 - ответ при передаче адреса отправителя
	:250 - ответ при передаче адреса получателя
	:354 - ответ на запрос "DATA"
	*/	
bool start_wait_word (char * word, uint16_t time, uint16_t len_, uint8_t buf_[]) {				
               uint8_t i, count_answ = 0;
//							 uint8_t strword[36];
//							 char *strword__ = "Timeout wait Word ";
               bool ok = true;
               len_word = strlen (word);
	
//							 for(i = 0; i<18; i++){ strword[i] = *strword__; strword__++; }	
               for (i = 0; i<len_word; i++) {
                              receive_word[i] = *word; 
															strword[i+18] = receive_word[i];
															word++; 
               }
							 strword[len_word+18] = '!'; strword[len_word+19] = '\0'; 
               count_len_word = 0;
               wait_word = true;
							 if (len_ !=0 ) { 
//									send_str_USB((char*)buf_, len_);
									send_CIPSEND_mail(len_, buf_);
							 };;
               while (!wait_word_got) {
                              DelaymS (100); count_answ++; 
                              if (count_answ> time/100) {
                                            get_strBUF_USB (strword); 
                                            ok = false;           
                                            break;                  
                              }
               }
               wait_word = false;
               wait_word_got = false;
               return ok;
}
//////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////
void getstr_BUF_USB (uint8_t *s)  // передаем в USB символы строки, не более 57 за пакет
{
	uint16_t i, j, count_s = 0;

		while (*s != '\0')
		{
			count_s++; s++;
		}
		s = s - count_s;
				for (j = 0; j < count_s/57; j++) {								// максимально по USB за одну посылку передаем по 57 байт, если пришло больше, то в цикле формируем несколько пакетов
						for (i = 0; i<57; i++) {
							Send_Buffer[i+7] = *s; s++;
						}
					  Send_Buffer[4] = 0x74; Send_Buffer[6] = 57;
					  data_answer();	DelaymS (100);
				}
				if (count_s%57 !=0) {
							for (i = 0; i<57; i++) {
								Send_Buffer[i+7] = *s; s++;
							}
						  Send_Buffer[4] = 0x74; Send_Buffer[6] = count_s%57;
						  data_answer();
//						DelaymS(100);
  			}

}
///////////////////////////////////////////////////////
///////////////////////////////////////////////////////
void send_str_USB (char *s, uint8_t count) {				// отправка  небольшой строки( до 56 байт) и одного байта данных (числа не более 255)
	int i = 7; int val = 1;

//	if(debug_Transmit_USB){
		while (*s != '\0') {
			Send_Buffer[i] = *s;
			s++; i++;
			if (i>55) break;
		}
	/*
	if(count/10==0)	{
		Send_Buffer[i] = '0'+ count;
		Send_Buffer[6] = i-6;
	}
	else{
		Send_Buffer[i] = '0'+ count;
	}*/
	if (count/100 != 0) { val = 3; goto ex; }
	if (count/10 != 0) { val = 2; goto ex; }
	ex:	
		switch (val) {
			case 1:
				Send_Buffer[i] = '0'+ count; Send_Buffer[6] = i-6;
			break;
			case 2:
				Send_Buffer[i] = '0'+ count/10; Send_Buffer[i+1] = '0'+ count%10; 
				Send_Buffer[6] = i-5;
			break;
			case 3:
				Send_Buffer[i] = '0'+ count/100; Send_Buffer[i+1] = '0'+ ((count%100)/10); Send_Buffer[i+2] = '0'+ count%10; 
				Send_Buffer[6] = i-4;			
			break;
		}
		Send_Buffer[4] = 0x74;  
//		if(debug_Transmit_USB) { data_answer(); };
		data_answer();

//	}
}
///////////////////////////////////////////////////////
/////////////////////////ожидание ответа ОК от модуля
bool wait_OK (void) {
	uint16_t count = 0;
	while (!flag_OK_comWIFI) {
		count++; DelaymS (1);
		if (count > 100) break;
	}
		return flag_OK_comWIFI;
}
///////////////////////////////////////////////////////
//////////////////////////////сброс всех сохраненных соединений
void reset_exist_connect(void) {
	uint8_t i = 0;
	for (i = 0; i<10; i++) {
		exist_connect[i] = false;
	}
	all_number_connect_WIFI = 0;
}
///////////////////проверка существующих соединений
bool check_exist_CON (uint8_t num) {
	bool ok = true;
	if (!exist_connect[num]) { exist_connect[num] = true; ok = false; }
	return ok;
}
//////////////////////////////////////////////////////////////////////////////////////////////
void led_one_ON (uint16_t led_);
//////////////////// по прерываниям закидываем в функцию приходящий байт и его номер в массиве
void recieved_word (unsigned char RXc, uint16_t count_in) {			/// по прерываниям закидываем в функцию приходящий байт и его номер в массиве
			///////////////////////////////		отслеживаем приход служебных слов

	if ( RXc == init_param.status[count_status_word]) { count_status_word++; }  // сравниваем полученный байт с содержимым в нулевом элементе массива, при совпадении инкрементируем счетчик
			else { count_status_word = 0; }
			if ( count_status_word == 8) { flag_getESP8266 = com_status; }					/// получен ответ на запрос статуса
////////////////////////////////////				
			if ( RXc == init_param.flag_recieved[count_getdata_word]) { count_getdata_word++; }
			else { count_getdata_word = 0; }
			if ( count_getdata_word == 5) { 																/// получены данные, начало данных в массиве in_comPORT начиная с байта count_start_dataWIFI
				flag_getESP8266 = com_get_data; count_start_dataWIFI = count_in; //count_start_dataWIFI = count_in+5; 
			}					
////////////////////////////////////	
			if ( RXc == init_param.flag_busy[count_getbusy_word]) { count_getbusy_word++; }
			else { count_getbusy_word = 0; }
			if ( count_getbusy_word == 8) { flag_getESP8266 = com_get_busy; flag_make_end = false; }					/// получен флаг занятости
////////////////////////////////////				
			if ( RXc == init_param.flag_connected[count_getconnect_word]) { count_getconnect_word++; }
			else { count_getconnect_word = 0; }
			if( count_getconnect_word == 8) { 																												/// соединение установлено
				flag_getESP8266 = com_get_connect; 
				flag_make_end = false; 
				if (!single_connection) {																																	// если не выбрано одиночное подключение
					number_connect_WIFI = in_comPORT[count_in-8] - 48;  	// 48 - код нуля
					if (!check_exist_CON(number_connect_WIFI)) { 															// для исключения ошибки подсчета кол-ва соединений
						all_number_connect_WIFI++; ////send_strUSB("Connect WIFI");
					}
				}
			}					
////////////////////////////////////			
			if ( RXc == init_param.flag_closed[count_getclosed_word]) { count_getclosed_word++; }			/// соединение закрыто
			else { count_getclosed_word = 0; }
			if ( count_getclosed_word == 7) { 
				flag_getESP8266 = com_get_closed; 
				flag_make_end = false; 
				if (!single_connection) {
					number_connect_WIFI = in_comPORT[count_in-7] - 48;  	// 48 - код нуля
					if (check_exist_CON (number_connect_WIFI)) { 																							 // если соединение было, то закрываем его
						if (all_number_connect_WIFI>0) all_number_connect_WIFI--; 
						exist_connect[number_connect_WIFI] = false; // send_strUSB("Disconnect WIFI");
					}
				}
			}				
			////////////////////////////////////	ожидаем готовности модуля к отправке данных 
			if (flag_wait_sendWIFI) {
				if (RXc=='>') {
					flag_end_sendWIFI = true; flag_wait_sendWIFI = false;
				} 						// приход символа '>' означает готовность модуля принять данные для передачи клиенту
			}
////////////////////////////////////	
			if ( RXc == init_param.flag_sendOK[count_get_sendOK]) { count_get_sendOK++; }
			else { count_get_sendOK = 0; }
			if ( count_get_sendOK == 8) { com_sendOK = true; }					/// получен флаг успешно отправленных данных
///////////////////////////////////////////
			if ( RXc == init_param.wifi_gotIP[count_gotIP]) { count_gotIP++; }
			else { count_gotIP = 0; }
			if ( count_gotIP == 11) { flag_gotIP = true; }					/// получен флаг получения IP в сети
////////////////////////////////////		
////////////////////////////////////			
			if ( RXc == init_param.req_IP[count_req_IP]) { count_req_IP++; }
			else { count_req_IP = 0; }
			if ( count_req_IP == 6) { 															/// получен флаг получения ответа на запрос IP в сети
				count_start_dataWIFI = count_in;
				flag_req_IP = true; led_one_ON(GPIO_Pin_9);
			};			
			if (flag_req_IP) { 																		/// получение IP адреса
				if (RXc == '"' && count_word_ip==0 ) { flag_start_count_wordIP = true; }
				if ( RXc == '"' && count_word_ip > 0) { ip_adr_getESP[count_word_ip] = RXc; flag_start_count_wordIP = false; count_word_ip = 0; flag_req_IP = false; flag_req_IP_end = true;}
				if (flag_start_count_wordIP) { ip_adr_getESP[count_word_ip] = RXc; count_word_ip++; }
						
			}
////////////////////////////////////			
////////////////////////////////////			
/*			if( RXc == init_param.req_MAC[count_req_MAC]) { count_req_MAC++; }
			else { count_req_MAC = 0; }
			if( count_req_MAC == 6) { flag_req_MAC = true; }					/// получен флаг получения ответа на запрос MAC adr*/
////////////////////////////////////				
			if ( RXc == init_param.lost_con[count_get_link_not_valid]) { count_get_link_not_valid++; }
			else { count_get_link_not_valid = 0; }
			if ( count_get_link_not_valid == 17) { flag_getESP8266 = com_get_link_not_valid; }					/// получен флаг обрыва соединения	
////////////////////////////////////	
			if ( RXc == str_error[count_bait_err]) { count_bait_err++; }
			else { count_bait_err = 0; }
			if ( count_bait_err == 5) { flag_er_command = true; }					/// получен флаг ошибки отправки команды модулю ESP
/////////////////////////////////////////////////			
      if (wait_word) {
         if ( RXc == receive_word[count_len_word]) { count_len_word++; }
         else { count_len_word = 0; }
         if ( count_len_word == len_word) { wait_word_got = true; }                                                                      /// 
       }  
/////////////////////////////////////////////////			
			RXc_last = RXc;
}
////////////////////////////////////////////////////////////////////////////

//////////////////////////////инициализация структуры параметров ESP8266
void init_paramESP8266(void){
	uint16_t i;
	char* ch;
//	char* ch_from64, *ch_pass64, *ch_from, *ch_to;
/*
	init_param.IP_adr[0] = 192; init_param.IP_adr[1] = 168; init_param.IP_adr[2] = 1; init_param.IP_adr[3] = 1;
//	init_param.SSID_wifi[0] = 'E'; init_param.SSID_wifi[1] = 't'; init_param.SSID_wifi[2] = 'a'; init_param.SSID_wifi[3] = 'n'; init_param.SSID_wifi[4] = 255;
	init_param.SSID_wifi[0] = 'e'; init_param.SSID_wifi[1] = 'z'; init_param.SSID_wifi[2] = '_'; init_param.SSID_wifi[3] = 'd'; init_param.SSID_wifi[4] = 'i'; 
	init_param.SSID_wifi[5] = 'r'; init_param.SSID_wifi[6] = '3'; init_param.SSID_wifi[7] = '0'; init_param.SSID_wifi[8] = '0'; init_param.SSID_wifi[9] = 255;
	len_nameSSID = 0;
	for(i = 0; i<16; i++){																			// определяем длину имени сети
		if(init_param.SSID_wifi[i]!= 255){ len_nameSSID++; }
		else break;
	}
//	ch = "4h-agK7&";  
	ch = "1969EvAn"; 
	for(i = 0; i<8; i++){	init_param.pass_wifi[i] = *ch; ch++;	}
	*/
	ch = "+IPD,";
	for (i = 0; i<5; i++) {	init_param.flag_recieved[i] = *ch; ch++; }	
	ch = " STATUS:";
	for (i = 0; i<8; i++) {	init_param.status[i] = *ch; ch++;	}	
	ch = " busy p.";
	for (i = 0; i<8; i++) {	init_param.flag_busy[i] = *ch; ch++;	}	
	ch = ",CONNECT";
	for (i = 0; i<8; i++) {	init_param.flag_connected[i] = *ch; ch++;	}	
	ch = ",CLOSED";
	for (i = 0; i<7; i++) {	init_param.flag_closed[i] = *ch; ch++; }
	ch = "AT+CIPSEND=";
	for (i = 0; i<11; i++) { init_param.command_sendwifi[i] = *ch; ch++; }
	ch = "\nSEND OK";
	for (i = 0; i<8; i++) { init_param.flag_sendOK[i] = *ch; ch++; }
/*	
	ch = "link is not valid";
	for(i = 0; i<17; i++){	init_param ->lost_con[i] = *ch; ch++;	}
*/	
	
	ch = "link is not valid";
	for (i = 0; i<17; i++) {	init_param.lost_con[i] = *ch; ch++;	}

	ch = "WIFI GOT IP";
	for (i = 0; i<11; i++) {	init_param.wifi_gotIP[i] = *ch; ch++;	}
	
	ch = ":STAIP";																								// ":STAIP"  - в режиме станции, и ":APIP" в режиме точки доступа
	for (i = 0; i<6; i++) {	init_param.req_IP[i] = *ch; ch++;	}
	
	ch = "STAMAC";																								// "STAMAC" - в режиме станции, и  ":APMAC" в режиме точки доступа		
	for (i = 0; i<6; i++) {	init_param.req_MAC[i] = *ch; ch++;	}
	
	init_param.MAC_adr[0] = 0x68; init_param.MAC_adr[1] = 0xC6; init_param.MAC_adr[2] = 0x3A; 
	init_param.MAC_adr[3] = 0xA7;	init_param.MAC_adr[4] = 0x8F; init_param.MAC_adr[5] = 0xED; 
	
	flag_getESP8266 = com_get_busy;
	init_param.potrserv = 8558;
	
//		account = acc_mail;
//	account = acc_yandex;
//	account = acc_google;
	// Для работы с почтой нужно сохранить параметры - ch_from64(логин почты в кодировке Base64 format), ch_pass64 (пароль почты в кодировке Base64 format)
	// ch_from(логин почты исх.), ch_to(логин почты получателя). В перспективе данные будут храниться в массивах mail_from64[i], mail_pass64[i],
	// 	mail_from[i], mail_to[i]
	/*
		switch(account){
		case acc_mail:
			ch_from64 = "ZXZhbjc3QGJrLnJ1"; ch_pass64 = "RXZBbjE5Njk="; ch_from = "evan77@bk.ru"; ch_to = "ezemtsov@bk.ru"; //ch_to = "ezemtsov@yandex.ru"; //
		break;
		case acc_yandex:
			ch_from64 = "ZXplbXRzb3ZAeWFuZGV4LnJ1"; ch_pass64 = "MDZhbnRyU1kwMw=="; ch_from = "ezemtsov@yandex.ru"; ch_to = "ezemtsov@bk.ru";
		break;
		case acc_google:
			ch_from64 = "dGVzdC5hbnRyc0BnbWFpbC5jb20="; ch_pass64 = "dGVzdGFzZGY="; ch_from = "test.antrs@gmail.com"; ch_to = "evan77@bk.ru";
		break;
	}

	mail_from_len64 = strlen(ch_from64);
	for(i = 0; i<mail_from_len64; i++){	mail_from64[i] = *ch_from64; ch_from64++;	}
	
	mail_pass_len64 = strlen(ch_pass64);
	for(i = 0; i<mail_pass_len64; i++){	mail_pass64[i] = *ch_pass64; ch_pass64++;	}
	
	ch = "MAIL FROM:<";
	for(i = 0; i<11; i++){	mail_from[i] = *ch; ch++;	}
	mail_from_len = strlen(ch_from);
	for(i = 0; i<mail_from_len; i++){	mail_from[i+11] = *ch_from; ch_from++;	}
	mail_from[mail_from_len+11] = '>';
	mail_from_len += 12;
	
	ch = "RCPT TO:<";
	for(i = 0; i<9; i++){	mail_to[i] = *ch; ch++;	}
	mail_to_len = strlen(ch_to);
	for(i = 0; i<mail_to_len; i++){	mail_to[i+9] = *ch_to; ch_to++;	}
	mail_to[mail_to_len+9] = '>';
	mail_to_len += 10;
	*/
}

/////////////////////////////////////////////////////
/////////////////////////
void init_WIFI_server(void){
	/*================================== LOG INIT_ESP8266
AT 
OK
AT+CWMODE=1 
OK
AT+CWJAP="Etan","4h-agK7&" WIFI DISCONNECT
WIFI CONNECTED
WIFI GOT IP

OK
AT+CIPMODE=0 
OK
AT+CIPMUX=1 
OK
AT+CIPSERVER=1,8888 
OK
0,CONNECT
	======================================*/
	uint8_t i;
	bool flag_fail_INIT = false;
	flag_initESP8266 = true;
	flag_OK_comWIFI = false;
	init_paramESP8266(); countIN_com = 0;
	//////////////////////
	SerialPutString ("AT"); SerialPutString ("\r\n"); DelaymS (50); flag_OK_comWIFI = false; SerialPutString ("AT"); SerialPutString ("\r\n");
	if (!wait_OK()) SerialPutString ("AT"); 
	
	if (len_nameSSID <17 ) { 																		// если имя сети меньше 17 символов, то считаем что есть в памяти сохраненные настройки и пытаемся подключиться к известной сети
		/////////выбор режима 1 — STA, 2 — AP, 3 — BOTH
		SerialPutString ("AT+CWMODE=1"); SerialPutString ("\r\n");
		if (!wait_OK()) {SerialPutString ("AT+CWMODE=1"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CWMODE!\n"); } DelaymS (50);
		if (!flag_OK_comWIFI) { flag_fail_INIT = true;}
	/////////подключение к сети	
		flag_OK_comWIFI = false; 
		SerialPutString ("AT+CWJAP=\""); //SerialPutString(SSID_wifi);
		for (i = 0; i<len_nameSSID; i++) { SerialPutChar (init_param.SSID_wifi[i]); }			// передаем модулю имя сети необходимой длины
		SerialPutString ("\",\""); 
		for (i = 0; i<8; i++){ SerialPutChar (init_param.pass_wifi[i]); } SerialPutChar ('"');
		SerialPutString ("\r\n"); 
		if (!wait_OK()) {
			if (flag_getESP8266 == com_get_busy) { 
				for (i = 0; i<12; i++) { DelaymS (900); if (wait_OK()) { flag_getESP8266 = com_no; break; }; };
			}
			if (!flag_OK_comWIFI) {
				SerialPutString ("AT+CWJAP=\""); //SerialPutString(SSID_wifi);
				for (i = 0; i<len_nameSSID; i++) { SerialPutChar (init_param.SSID_wifi[i]); }			// передаем модулю имя сети необходимой длины
				SerialPutString ("\",\""); 
				for (i = 0; i<8; i++){ SerialPutChar (init_param.pass_wifi[i]); } SerialPutChar ('"');
				SerialPutString ("\r\n");
				if (!wait_OK()) {
					if (flag_getESP8266 == com_get_busy) { 
						for (i = 0; i<12; i++) { DelaymS (900); if (wait_OK() ){ flag_getESP8266 = com_no; break; }; };
					}
				}
////////////////////////////////////////
				get_strBUF_USB ("Repeat send SSID!"); 
				if (!flag_OK_comWIFI) { flag_fail_INIT = true;}  // если всё-таки на повторную команду подключения к сети не пришел ответ ОК, то считаем инциализацию проваленой, выставляем flag_fail_INIT
				SerialPutString ("AT"); SerialPutString ("\r\n"); 
				i = 0; flag_OK_comWIFI = false;
				while (!wait_OK()) {
					SerialPutString ("AT"); SerialPutString ("\r\n"); 
					DelaymS (200); i++; 
					if (i> 40) {		//
						get_strBUF_USB ("wait answer for command OK..."); 
						break;					
					}
				}
////////////////////////////////////////			
			}
		}DelaymS (50);
	} else {																				// в памяти нет сохраненной сети, разворачиваем сеть "Termostat_EZAP"
//		SerialPutString ("AT+CWSAP_DEF=\"Termostat_EZAP\",\"1234567890\",5,0"); SerialPutString("\r\n");
		SerialPutString ("AT+CWSAP_DEF=\"Smart_Home_EZ\",\"1234567890\",5,0"); SerialPutString("\r\n");					//// Переходим к сети  Smart_Home_EZ
		DelaymS (150);
		SerialPutString ("AT+CWMODE=2"); SerialPutString ("\r\n");
		flag_OK_comWIFI = false;
		if (!wait_OK()) {SerialPutString ("AT+CWMODE=2"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CWMODE!\n"); } DelaymS (50);	
		if (!flag_OK_comWIFI) { flag_fail_INIT = true;}
	}
	/////////выбор режима сервера, mode = 0 — not data mode (сервер может отправлять данные клиенту и может принимать данные от клиента)
///////////mode = 1 — data mode (сервер не может отправлять данные клиенту, но может принимать данные от клиента)
	flag_OK_comWIFI = false; 
	SerialPutString ("AT+CIPMODE=0"); SerialPutString ("\r\n");
	if (!wait_OK()) {SerialPutString ("AT+CIPMODE=0"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CIPMODE=0!\n"); } DelaymS (50);
	if(!flag_OK_comWIFI) { flag_fail_INIT = true;}
	/////////устанавливаем возможность множественных подключений
	flag_OK_comWIFI = false; 
	SerialPutString ("AT+CIPMUX=1"); SerialPutString ("\r\n");
	if (!wait_OK()) {SerialPutString ("AT+CIPMUX=1"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CIPMUX=1!\n"); } DelaymS (50);
	if (!flag_OK_comWIFI) { flag_fail_INIT = true;}
	/////////запуск сервера на порту "port"
	flag_OK_comWIFI = false; 
	SerialPutString ("AT+CIPSERVER=1,"); send_integer (init_param.potrserv); SerialPutString ("\r\n");	
	if(!wait_OK()){SerialPutString("AT+CIPSERVER=1,"); send_integer(init_param.potrserv); SerialPutString("\r\n");	get_strBUF_USB("Repeat command AT+CIPSERVER=1!\n"); }DelaymS(50);
	if(!flag_OK_comWIFI) { flag_fail_INIT = true;}
//	SerialPutString(IP_adr);
	if( !flag_fail_INIT ) { get_strBUF_USB ("Success init ESP8266!"); modul_ESP_connect = true;}
	else { get_strBUF_USB ("Init ESP8266 FAIL!"); modul_ESP_connect = false; } 
	flag_initESP8266 = false; end_timeout_getdata = true; 
	SerialPutString("ATE0\r\n");														// отключение режима эхо
//	get_strBUF_USB(init_param ->link_not_valid);

}
//////////////////////////////////////////////////////////
void reinit_wifi_serv (void) {
	uint8_t k=0;
	
	SerialPutString ("AT+CWMODE=1"); SerialPutString ("\r\n");
	if (!wait_OK()) {SerialPutString ("AT+CWMODE=1"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CWMODE!\n"); k++; } DelaymS (50);
	flag_OK_comWIFI = false; 
	SerialPutString("AT+CIPMODE=0"); SerialPutString("\r\n");
	if (!wait_OK()) {SerialPutString ("AT+CIPMODE=0"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CIPMODE=0!\n"); k++; }DelaymS (50);
	/////////устанавливаем возможность множественных подключений
	flag_OK_comWIFI = false; 
	SerialPutString ("AT+CIPMUX=1"); SerialPutString ("\r\n");
	if(!wait_OK()){SerialPutString ("AT+CIPMUX=1"); SerialPutString ("\r\n"); get_strBUF_USB ("Repeat command AT+CIPMUX=1!\n"); k++; }DelaymS (50);
	/////////запуск сервера на порту "port"
	flag_OK_comWIFI = false; 
	SerialPutString ("AT+CIPSERVER=1,"); send_integer (init_param.potrserv); SerialPutString ("\r\n");	
	if (!wait_OK()) {SerialPutString ("AT+CIPSERVER=1,"); send_integer (init_param.potrserv); SerialPutString ("\r\n");	get_strBUF_USB ("Repeat command AT+CIPSERVER=1!\n"); k++; }DelaymS (50);
//	SerialPutString(IP_adr);
	if ( k< 3) { get_strBUF_USB ("Success init ESP8266!"); modul_ESP_connect = true;}
	else { get_strBUF_USB ("Init ESP8266 FAIL!"); modul_ESP_connect = false; } 
	flag_initESP8266 = false; end_timeout_getdata = true;  
	SerialPutString ("ATE0\r\n");														// отключение режима эхо
}
//////////////////////////////////////////////////////////
void sendUSB_getCOM_data (uint16_t count_byte, uint16_t start) {		// отправляем строку (кол-во байт "count_byte") в USB c адресом первого элемента "in_comPORT[start]"
	uint8_t* c;
	in_comPORT[start+count_byte] = '\0';														// записываем маркер окрнчания строки
	c = &in_comPORT[start];
	get_strBUF_USB (c);
}
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
uint16_t size_get_bytes_from_com (uint16_t start) {						// функция определения кол-ва принятых байт
	uint16_t size = 0, tmp = 1;
	while (in_comPORT[start+tmp]!= ':') {		// определяем порядок числа принятых байт
		tmp++;
		if (tmp>15) { size = 3; break;}
	}
//	send_str_USB("size_get_bytes_from_com = ", tmp);
	switch (tmp) {

		case 2:																									// получено однозначное число	при одиночном подключении
			size = in_comPORT[start+tmp-1]-48;
			count_start_dataWIFI = count_start_dataWIFI+3;
		break;
		case 3:																									// получено двухзначное число	при одиночном подключении
			size = (in_comPORT[start+tmp-2]-48)*10;
			size = size + (in_comPORT[start+tmp-1]-48);
			count_start_dataWIFI = count_start_dataWIFI+4;
		break;
//////////////////////////////////////////////////////////////////////////////		
		case 4:																									
			if (!single_connection) {																// получено однозначное число	
				size = in_comPORT[start+tmp-1]-48;
				count_start_dataWIFI = count_start_dataWIFI+5;
			} else {																								// получено трехзначное число	при одиночном подключении
				size = (in_comPORT[start+tmp-3]-48)*100;
				size = size + (in_comPORT[start+tmp-2]-48)*10;		
				size = size + in_comPORT[start+tmp-1]-48;		
				count_start_dataWIFI = count_start_dataWIFI+5;			
			}
		break;
		case 5:																									// получено двухзначное число	
			size = (in_comPORT[start+tmp-2]-48)*10;
			size = size + (in_comPORT[start+tmp-1]-48);
			count_start_dataWIFI = count_start_dataWIFI+6;
		break;
		case 6:																									// получено трехзначное число	 
			size = (in_comPORT[start+tmp-3]-48)*100;
			size = size + (in_comPORT[start+tmp-2]-48)*10;		
			size = size + in_comPORT[start+tmp-1]-48;		
			count_start_dataWIFI = count_start_dataWIFI+7;
		break;
	}
	if (!single_connection) number_connect_WIFI = in_comPORT[start+1] - 48;					// определяем номер текущего соединения WIFI
	return size;
	
}
//////////////////////////////////////////////////////
void send_data_clientWIFI (uint8_t buf[], uint16_t len) {
	uint16_t i;
			for (i = 0; i<len; i++) {
				SerialPutChar(buf[i]);
			}
//			SerialPutString("\r\n");
}
/////////////////////////////////////////////////////////////////////////////
void reset_ESP8266(void);
///////////////////////////отправка данных клиенту WIFI
void ready_send_dataWIFI (uint8_t con, uint8_t len, uint8_t buf_send[]) {
		uint8_t i, val = 1, count = 0;
	
			flag_start_sendWIFI = false;																	// в основном цикле по этому флагу идет сбор данных и отпрака TCP клиенту	
			flag_wait_sendWIFI = true;																		// флаг по сбросу которого модуль начинает прием данных для передачи TCP клиенту
		if (con < 9) {																											// соединение не может быть больше 10
			for (i = 0; i<11; i++) {
				SerialPutChar (init_param.command_sendwifi[i]);							// отправка модулю команды на передачу данных
			}	
			SerialPutChar ('0'+con); SerialPutChar (',');
			if (len/100 != 0 ){ val = 3; goto exx;}
			if (len/10 != 0)	{ val = 2; goto exx;}
	exx:	
			switch (val) {
				case 1:
					SerialPutChar ('0'+ len); 
				break;
			case 2:
					SerialPutChar ('0'+ len/10); SerialPutChar ('0'+ len%10); 
				break;
			case 3:
					SerialPutChar ('0'+ len/100); SerialPutChar ('0'+ ((len%100)/10)); SerialPutChar ('0'+ len%10); 
				break;
			}
			SerialPutString ("\r\n");
			
			while (!flag_end_sendWIFI) {											// ждем флага готовности модуля к передачи данных клиенту 2,5сек
			count++; DelaymS(10); 
				if (count>250) {
					if (!execute_new_net) modul_ESP_connect = false;										// сбой передачи, запускаем счетчик для сброса модуля
					break;
				}
			}
			if (count<250) {
				send_data_clientWIFI (buf_send, len_packet_COM);
				modul_ESP_connect = true;
				DelaymS(800);
			} else { 
				get_strBUF_USB ("Can not transmit data!!!"); 
//				reset_ESP8266(); DelaymS(1000);init_WIFI_server();
					modul_ESP_connect = false;
					reset_exist_connect();	
//				if(flag_getESP8266 == com_get_busy){ DelaymS(150); get_strBUF_USB("flag_getESP8266 = BUSY"); }
//				else{ DelaymS(150); send_str_USB("flag_getESP8266  = ", flag_getESP8266); }
				for (i = 0; i<15; i++) { 
					flag_OK_comWIFI = false; SerialPutString ("AT"); SerialPutString ("\r\n"); DelaymS (300); 
					if (wait_OK()) { send_str_USB("wait_OK,BREAK! i = ", i); break; }; 
					send_str_USB ("wait_OK, i = ", i); 
				}
				flag_OK_comWIFI = false;
			}
			flag_end_sendWIFI = false;
			if (!debug_Transmit_USB) flag_start_sendWIFI = true;
			DelaymS (100);
			send_str_USB ("WIFI connection number:", con);
			DelaymS (100);
		} else {
			DelaymS (100);
			send_str_USB ("error numer connection, con = ", con);
		}
/*
		send_str_USB("WIFI connection number:", con);
		DelaymS(200);
		send_str_USB("Number bytes for transmit:", len);*/
}
//void set_newNET(char* net, char* pass, uint32_t len){
void save_net (uint8_t data[], uint8_t flag, uint16_t len);
void set_newNET (uint8_t dim[]) {
	uint8_t i=0, j= 0, k = 0;
//	uint16_t start = 0;
	uint8_t buf[40];
	uint8_t buf_tmp[3];
	uint8_t set_link = 0x11;	
//	char*	chr;	//
//	uint8_t buffer[30];
	uint16_t count_answ = 0;

//	SerialPutString("AT+CWMODE=2"); SerialPutString("\r\n"); DelaymS(50);
	SerialPutString ("AT+CWMODE=1"); SerialPutString ("\r\n"); DelaymS (50);
	if (!wait_OK()) { SerialPutString("AT+CWMODE=1"); SerialPutString("\r\n"); } DelaymS(50);
	SerialPutString ("AT+CWJAP=\"");	for (i = 35; i < (dim[51]+35); i++) { SerialPutChar (dim[i]); }																// задаем имя сети
	SerialPutString ("\",\""); for (i = 52; i < 60; i++) { SerialPutChar (dim[i]); } SerialPutChar ('"'); SerialPutString ("\r\n"); DelaymS(50);  // задаем пароль
	///ждем подтверждения подключения
	while (!flag_gotIP) { 
		DelaymS (50); count_answ++; 
		if (count_answ> 500) {
			get_strBUF_USB ("No connection with new NET!"); 
			break;
		}
	} /// если больше 3 сек. нет ответа, выходим из цикла
	///запрашиваем IP адр.
	if (flag_gotIP) { get_strBUF_USB ("ESP_connect new NET!");};
	flag_gotIP = false;
	DelaymS (5000);
	SerialPutString ("AT+CIFSR"); SerialPutString ("\r\n"); DelaymS (50);
	count_answ = 0;
	while (!flag_req_IP_end) {
		DelaymS (50); count_answ++; 
		if (count_answ == 40) {
			SerialPutString ("AT+CIFSR"); SerialPutString ("\r\n"); 
			send_str_USB ("repeat req IP ", count_answ); 
		}
		if (count_answ == 200) {
			SerialPutString ("AT+CIFSR"); SerialPutString ("\r\n"); 
			send_str_USB (" repeat req IP ", count_answ); 
		}
		if (count_answ == 450) {
			SerialPutString ("AT+CIFSR"); SerialPutString ("\r\n"); 
			send_str_USB ("repeat req IP ", count_answ); 
		}
		if (count_answ> 500) {
			get_strBUF_USB ("No answer for req IP!"); 
			break;
		}
	}

	if (flag_req_IP_end) { send_str_USB ("in_com_start_byte ", count_start_dataWIFI); DelaymS (150);}
	

	///////////////////////////////////////////////////	
	
  for (i = 1; i<16; i++) {											// парсим полученные данные
    if (ip_adr_getESP[i] != '.' && ip_adr_getESP[i] !='"') { buf_tmp[k] = ip_adr_getESP[i]; k++; }
    else {
      switch (k) {
        case 1:
          buf[j] = '0'; buf[j+1] = '0'; buf[j+2] = buf_tmp[k-1];
        break;
        case 2:
          buf[j] = '0'; buf[j+1] = buf_tmp[k-2]; buf[j+2] = buf_tmp[k-1];
        break;
        case 3:
          buf[j] = buf_tmp[k-3]; buf[j+1] = buf_tmp[k-2]; buf[j+2] = buf_tmp[k-1];
        break;
      }
      k = 0; j+=3;
    }
    if (ip_adr_getESP[i]=='"') break;
  }	
  for (i = 0; i<12; i++) { init_param.IP_adr[i] = buf[i]; }				// забрасываем полученный IP в массив для отправки приложению

/////записываем параметры порта
	acs2 (init_param.potrserv, 4);
	for (i = 0; i<4; i++) { data_OUT_COM[i+31] = acs2_data[i]; }
//////////////////////////////////////////////////////
	for (i = 0; i < 16; i++) { buf[i] = dim[i+35]; }				// переписываем  в массив buf имя сети
	for (i = 0; i<8; i++) {	buf[i+16] = dim[i+52]; }				// переписываем  в массив ключь сети
	buf[24] = dim[51];																		// сохраняем в массиве buf длину имени сети
  for (i = 0; i<12; i++) {	buf[i+25] = init_param.IP_adr[i]; }				// переписываем  в массив IP адрес
//////////////////////////////////////////////////////
	if (flag_req_IP_end) save_net (buf, set_link, 40);							// сохраняем параметры сети в памяти МК если был получен успешный ответ от ESP
	DelaymS (150);
	flag_req_IP_end = false;
//	SerialPutString("AT+CWMODE=2"); SerialPutString("\r\n"); DelaymS(50); // включаем сеть Termostat_EZAP
//	if(!wait_OK()){ SerialPutString("AT+CWMODE=2"); SerialPutString("\r\n"); get_strBUF_USB("repeat on net Termostat_EZAP!");} DelaymS(50);
	SerialPutString ("AT+CWMODE=2"); SerialPutString ("\r\n"); DelaymS (50); // включаем сеть Smart_Home_EZ
	if (!wait_OK()) { SerialPutString ("AT+CWMODE=2"); SerialPutString ("\r\n"); get_strBUF_USB ("repeat on net Smart_Home_EZ!");} DelaymS (50);		//// Переходим к сети  Smart_Home_EZ
	flag_getESP8266 = com_no;
	reset_exist_connect();
	
	flag_start_sendWIFI = true;
//	get_strBUF_USB("set_newNET"); 
//	send_str_USB("set_newNET ", buf[24]);DelaymS(150);
//	get_strBUF_USB(buf); DelaymS(150);
//	execute_new_net = false;
}
///////////////////////////////////////////
///////////////////////////////////////////
void out_echo_from_ESP (void);
void send_CIPSEND_mail (uint16_t len, uint8_t buf_send[]) {
		uint8_t val = 1;
		uint16_t count = 0;
		uint8_t flag_debug = 0;
		len +=2;																		// +2 для символов возврата каретки и окончания строки
//			flag_start_sendWIFI = false;
			flag_wait_sendWIFI = true;
			SerialPutString ("AT+CIPSEND=");
	if (len/1000 != 0 ) { val = 4; goto exx;}		// длинна сообщения укладывается в четырехзначное число
	else {
		if (len/100 != 0 ) { val = 3; goto exx;}		// длинна сообщения укладывается в трехзначное число
		else {
			if (len/10 != 0) { val = 2; goto exx;} // длинна сообщения укладывается в двухзначное число
		}
	}
	exx:	
			switch (val) {
				case 1:
					SerialPutChar ('0'+ len); flag_debug = 1;
				break;
			case 2:
					SerialPutChar ('0'+ len/10); SerialPutChar ('0'+ len%10);  flag_debug = 2;
				break;
			case 3:
					SerialPutChar ('0'+ len/100); SerialPutChar ('0'+ ((len%100)/10)); SerialPutChar ('0'+ len%10); flag_debug = 3;
				break;
			case 4:
					SerialPutChar ('0'+ len/1000); SerialPutChar ('0'+ ((len%1000)/100)); SerialPutChar ('0'+ ((len%100)/10)); SerialPutChar ('0'+ len%10); flag_debug = 4;
				break;
			}
			SerialPutString ("\r\n");
			
			while (!flag_end_sendWIFI) {											// ждем флага готовности модуля к передачи данных клиенту 2.5сек
			count++; DelaymS (10); 
				if (count>250) {
						modul_ESP_connect = false;										// сбой передачи, запускаем счетчик для сброса модуля
						send_str_USB ("time out! count>250, flag_debug=", flag_debug);
					break;
				}
			}
			if (count<250) {
				send_data_clientWIFI (buf_send, len-2);
				SerialPutString ("\r\n");
				modul_ESP_connect = true;
//				DelaymS(100);
			} else { get_strBUF_USB ("Can not transmit data for MAIL!"); }
			
			flag_end_sendWIFI = false;
//			if(!debug_Transmit_USB)flag_start_sendWIFI = true;
}

///////////////////////////////////////////
///////////////////////////////////////////

bool programm_resetESP(void) {
	uint16_t count_answ = 0;
	bool rr = false;
	flag_gotIP = false;
	SerialPutString ("AT+RST"); SerialPutString ("\r\n"); DelaymS (1000);
	while (!flag_gotIP) {
		DelaymS (100);
		count_answ++; 
		if (count_answ> 130) {
			get_strBUF_USB ("IP adr did not get!"); 
			break;
		}
	}
	rr = flag_gotIP;
	if (rr) send_str_USB ("IP adr got success! ", count_answ);
	flag_gotIP = false;
	DelaymS (1500);
	SerialPutString ("AT"); SerialPutString ("\r\n"); DelaymS (50);
	return rr;
}

//////////////////////////////////////////////

void send_MAIL (char* txt_mail_str, char* sub_mail, bool from_dim_txt) {
///////////////////////////////////////////////
/*				  send_str_USB("mail_from_len = ", mail_from_len);					
				DelaymS(150);
				get_strBUF_USB(mail_from);
				DelaymS(150);
					send_str_USB("mail_to_len = ", mail_to_len);					
				DelaymS(150);
				get_strBUF_USB(mail_to);
				DelaymS(150);
					send_str_USB("mail_from_len64 = ", mail_from_len64);					
				DelaymS(150);
				get_strBUF_USB(mail_from64);
				DelaymS(150);	
					send_str_USB("mail_pass_len64 = ", mail_pass_len64);					
				DelaymS(150);
				get_strBUF_USB(mail_pass64);
				DelaymS(150);		*/
///////////////////////////////////////////////	
	
	uint16_t count_answ = 0;
	uint16_t delay_pack = 500;
	uint8_t count_rep = 0; 				// число повторов отправки письма;
	char* ch;
	uint8_t symb[850];
	uint16_t i;
	uint16_t len_char= 0;
	bool connect;
	rep:	
//////////////////////////////////////////////////////////////////	
	single_connection = true; number_connect_WIFI = 0;
	connect = true;
	reset_ESP8266();
	programm_resetESP();
	//////////////////////////////
	flag_OK_comWIFI = false;
	DelaymS (delay_pack);
	countIN_com = 0;	// устанавливаем буфер ком на 0, для удобства последующего чтения
	SerialPutString ("AT+CIPMUX=0"); SerialPutString ("\r\n"); 	// переводим в режим одного подключения
	if (!wait_OK()) { SerialPutString ("AT+CIPMUX=0"); SerialPutString ("\r\n"); get_strBUF_USB ("rep. com MUX"); DelaymS (50);}
	flag_OK_comWIFI = false;
	SerialPutString ("AT+CIPSSLSIZE=4096"); SerialPutString ("\r\n");		// увеличиваем размер буфера для приема данных
	if (!wait_OK()) { SerialPutString ("AT+CIPSSLSIZE=4096"); SerialPutString ("\r\n"); DelaymS (50);}
/*	switch (account_mail) {
		case acc_mail:
//			SerialPutString("AT+CIPSTART=\"SSL\",\"smtp.bk.ru\",465"); SerialPutString("\r\n");		// подключаемся к серверу bk.ru
			SerialPutString ("AT+CIPSTART=\"SSL\",\"smtp.mail.ru\",465"); SerialPutString ("\r\n");		// подключаемся к серверу mail.ru
		break;
		case acc_yandex:
			SerialPutString ("AT+CIPSTART=\"SSL\",\"smtp.yandex.ru\",465"); SerialPutString ("\r\n");		// подключаемся к серверу yandex.ru
		break;
		case acc_google:
			SerialPutString ("AT+CIPSTART=\"SSL\",\"smtp.gmail.com\",465"); SerialPutString ("\r\n");		// подключаемся к серверу yandex.ru
		break;
	}*/
  
  
  ch = "AT+CIPSTART=\"SSL\",\"";
  for (i = 0; i<19; ++i) {
    symb[i] = *ch; ch++;
  }
  for (i = 0; i<mail_name_serv_len; ++i) symb[i+19] = mail_name_serv[i];
  symb[mail_name_serv_len+19] = '"';
  symb[mail_name_serv_len+1+19] = ',';
  symb[mail_name_serv_len+2+19] = mail_name_port[0];
  symb[mail_name_serv_len+3+19] = mail_name_port[1];
  symb[mail_name_serv_len+4+19] = mail_name_port[2];
  symb[mail_name_serv_len+5+19] = '\0';
  SerialPutString (symb);
  SerialPutString ("\r\n");
	out_echo_from_ESP();							// эхо от ESP для дебага
  //DelaymS (delay_pack); 
  
	while (flag_getESP8266 == com_get_connect) { 
		DelaymS (50); count_answ++; 
		if (count_answ> 500) {
			get_strBUF_USB ("No connection with SMTP server!"); 
			connect = false; // for debug 05.07.2018
			break;
		}
	} /// если больше 25 сек. нет ответа, выходим из цикла
	count_answ = 0;
	
//	if(connect){ get_strBUF_USB("Connection with SMTP server!"); }
	com_sendOK = false;
	if (connect) {
		if (start_wait_word (":220", 10000, 0, 0)) { ; }
		else { connect = false; goto ex_er; }

		DelaymS (delay_pack);
		
		ch = "Helo ";										// IP адрес содержится в буфере buf_WIFI начиная с 19 байта, 12 символов без точек
		for (i = 0; i<5; i++) {	symb[i] = *ch; ch++;	}
		for (i = 0; i<3; i++) {	symb[i+5] = data_OUT_COM[i+19]; }		symb[8] = '.';
		for (i = 0; i<3; i++) {	symb[i+9] = data_OUT_COM[i+22]; }		symb[12] = '.';
		for (i = 0; i<3; i++) {	symb[i+13] = data_OUT_COM[i+25]; }	symb[16] = '.';	
		for (i = 0; i<3; i++) {	symb[i+17] = data_OUT_COM[i+28]; } symb[20] = '\0';
		len_char = 20;
		if (start_wait_word(":250", 5000, len_char, symb)) { ; }
		else { connect = false; goto ex_er; }
		com_sendOK = false;
		out_echo_from_ESP();					// эхо от ESP для дебага
	}
	count_answ = 0;

////////////////////////////////
	if (connect) {
		DelaymS (delay_pack);
		ch = "AUTH LOGIN";  
		len_char = strlen (ch);
		for (i = 0; i<len_char; i++) {	symb[i] = *ch; ch++;	}
//		send_CIPSEND_mail(len_char, symb);
		
		if (start_wait_word (":334", 5000, len_char, symb)) { ; }
		else { connect = false; goto ex_er; }
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}	
	count_answ = 0;

	////////////////////////////////
	if (connect) {
		DelaymS (delay_pack);
//		send_CIPSEND_mail(mail_from_len64, mail_from64);		

		if (start_wait_word (":334", 10000, mail_from_len64, mail_from64)) { ; }
		else { connect = false; goto ex_er; }
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}	
	count_answ = 0;
		////////////////////////////////
	
	if (connect) {
		DelaymS (delay_pack);
//		send_CIPSEND_mail(mail_pass_len64, mail_pass64);		

		if (start_wait_word (":235", 5000, mail_pass_len64, mail_pass64)) { ; }
		else { connect = false; goto ex_er; }
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}	
	count_answ = 0;
	
		////////////////////////////////

	if (connect) {
		DelaymS (delay_pack);
//		send_CIPSEND_mail(mail_from_len, mail_from);	

		if (start_wait_word (":250", 5000, mail_from_len, mail_from)) { ; }
		else { connect = false; goto ex_er; }
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}		
	count_answ = 0;
			////////////////////////////////
		
	if (connect) {
		DelaymS (delay_pack);
//		send_CIPSEND_mail(mail_to_len, mail_to);	

		if (start_wait_word (":250", 5000, mail_to_len, mail_to)) { ; }
		else { connect = false; goto ex_er; }		
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}	
	count_answ = 0;
				////////////////////////////////
	
	if (connect) {
		DelaymS (delay_pack);
		ch = "DATA";  
		len_char = strlen (ch);
		for (i = 0; i<len_char; i++) {	symb[i] = *ch; ch++;	}
//		send_CIPSEND_mail(len_char, symb);
		
		if (start_wait_word (":354", 5000, len_char, symb)) { ; }
		else { connect = false; goto ex_er; }
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}
	count_answ = 0;
					////////////////////////////////
								
	if (connect) {
		DelaymS (delay_pack);
		ch = sub_mail;
		len_char = strlen(ch);
		for (i = 0; i<len_char; i++) {	symb[i] = *ch; ch++;	}
		send_CIPSEND_mail (len_char, symb);
		while (!com_sendOK) {
			DelaymS (100); count_answ++; 
      if (count_answ> 10) {
				get_strBUF_USB ("No answer for req SUBJ!"); 
				connect = false;	
				break;					
			}
		}
		com_sendOK = false;
		out_echo_from_ESP();						// эхо от ESP для дебага
	}
	count_answ = 0;
								////////////////////////////////
	
		if (connect) {
		DelaymS (delay_pack);
		ch = "To: AppD";																								// заменяем в массиве первые 8 символов
		for (i = 0; i<8; i++) {	symb[i] = *ch; ch++;	}
		for (i = 8; i<mail_to_len; i++) {	symb[i] = mail_to[i]; }
		send_CIPSEND_mail (mail_to_len, symb);		
		
		while (!com_sendOK) {
			DelaymS (100); count_answ++; 
      if (count_answ> 10) {
				get_strBUF_USB("No answer for req TO!"); 
//				send_str_USB("No answer for req TO! ", mail_to_len);					
//				DelaymS(150);
//				get_strBUF_USB(symb);					
				connect = false;	
				break;					
			}
		}
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}
	count_answ = 0;
							////////////////////////////////
	
	if (connect) {
		DelaymS (delay_pack);
		ch = "From: Evan";																								// заменяем в массиве первые 10 символов
		for (i = 0; i<10; i++) {	symb[i] = *ch; ch++;	}
		for (i = 10; i<mail_from_len; i++) {	symb[i] = mail_from[i]; }
		send_CIPSEND_mail (mail_from_len, symb);
		while (!com_sendOK) {
			DelaymS (100); count_answ++; 
      if (count_answ> 10) {
				get_strBUF_USB ("No answer for req FROM!"); 
				connect = false;	
				break;					
			}
		}
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}
	count_answ = 0;
						////////////////////////////////

	if (connect) {
		DelaymS (delay_pack);
		if (!from_dim_txt) { 							//если передаем текст при вызове функции
			ch = txt_mail_str;
			len_char = strlen (ch);
			for (i = 0; i<len_char; i++) {	symb[i] = *ch; ch++;	}
			send_CIPSEND_mail (len_char, symb);
		} else {
			for (i = 0; txt_mail[i]!='\0'; i++) {	;	}
			send_CIPSEND_mail (i, txt_mail);
		}
		
		while (!com_sendOK) {
			DelaymS (100); count_answ++; 
      if (count_answ> 300) {
				get_strBUF_USB("No answer for req MESS!"); 
				connect = false;	
				break;					
			}
		}
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}
	count_answ = 0;
									////////////////////////////////
	if (connect) {
		DelaymS (delay_pack);
		symb[0] = '.';
		send_CIPSEND_mail (1, symb);
		while (!com_sendOK) {
			DelaymS (100); count_answ++; 
      if (count_answ> 10) {
				get_strBUF_USB ("No answer for req \".\""); 
				connect = false;	
				break;					
			}
		}
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}
	count_answ = 0;
								////////////////////////////////
	if (connect) {
		DelaymS (delay_pack);
		ch = "QUIT";  
		len_char = strlen (ch);
		for (i = 0; i<len_char; i++) {	symb[i] = *ch; ch++;	}
		send_CIPSEND_mail (len_char, symb);
		
		while (!com_sendOK) {
			DelaymS (100); count_answ++; 
      if (count_answ> 10) {
				get_strBUF_USB ("No answer for req QUIT!"); 
				connect = false;		
				break;					
			}
		}
		com_sendOK = false;
		out_echo_from_ESP();							// эхо от ESP для дебага
	}
	count_answ = 0;
ex_er:	
	if (!connect && count_rep<4) {										// 5 попыток отправить письмо						
		count_rep++;
		SerialPutString ("AT+CIPCLOSE"); DelaymS (50);
		send_str_USB ("mail not send! Try ", count_rep+1);
		goto rep;
	} else {
		if (count_rep<5 && connect) { send_str_USB ("Mail sent success! ", count_rep+1);}
	}
//	programm_resetESP();
		DelaymS (delay_pack);
		single_connection = false; 
//		reinit_wifi_serv();
		if (count_rep>1) reset_ESP8266();;
		programm_resetESP();
		init_WIFI_server();
//	SerialPutString("AT+CIPCLOSE");
//	DelaymS(delay_pack);
///////////////////////////////////	

}
//////////////////////////////////////////////

////////////////////////////////////////////
uint8_t func_acs2 (double k) {      // size_symb - кол-во символов числа , k - число 
  uint8_t before_p1, before_p2, before_p3, after_p1, after_p2;
  uint8_t num_symb_before, symb = 0, start_after; 
  char prefix;
  //char* rez;
  int tmp;

	if (k>0) { prefix = '+'; }
	else { 
		if (k<0) { prefix = '-'; k = -1*k; }
		else { prefix = '_'; }
	}
	acs2_symb[0] = prefix;

	if (k !=0) {
		tmp = (int)k;
		if (tmp != 0) {
			before_p1 = tmp%10; // находим младший разряд, единицы
			if (before_p1 != 0) num_symb_before = 1;
			before_p2 = (tmp%100)/10; // находим средний разряд, десятки
			if (before_p2 != 0) num_symb_before = 2; 
			tmp = (int)(k); // находим целую часть
			before_p3 = tmp/100; // находим старший разряд, сотни
			if (before_p3 != 0) num_symb_before = 3; 
			switch (num_symb_before) {
				case 1:
					acs2_symb[1] = '0'; 
					acs2_symb[2] = before_p1+ '0'; start_after = 4; num_symb_before = 2;
				break;
				case 2:
					acs2_symb[1] = before_p2+ '0';
					acs2_symb[2] = before_p1+ '0'; start_after = 4;
				break;
				case 3:
					acs2_symb[1] = before_p3+ '0';
					acs2_symb[2] = before_p2+ '0'; 
					acs2_symb[3] = before_p1+ '0'; start_after = 5; 
				break;
			}
		}	else {
				acs2_symb[1] = acs2_symb[0];
				acs2_symb[0] = '_'; 
				acs2_symb[2] = '0'; start_after = 4; num_symb_before = 2;
	  }
		tmp = (int)(k *100); /// если цифр после запятой будет больше 2, то они будут отброшены
		tmp = tmp%100;
	
		acs2_symb[start_after-1] = '.';
		after_p1 = tmp%10; // находим сотые доли
		after_p2 = (tmp - after_p1)/10; // находим десятые доли
		acs2_symb[start_after] = after_p2+ '0';
		acs2_symb[start_after+1] = after_p1+ '0'; 
	} else { acs2_symb[1] = '_'; acs2_symb[2] = '0'; acs2_symb[3] = '.'; acs2_symb[4] = '0'; acs2_symb[5] = '0'; num_symb_before = 2; }
	symb = 3 + num_symb_before+1;

	return symb;
}
////////////////////////////////////////////

///////////////////////////////////////////

void init_constTxt (void) {
  uint8_t k; 
  size_t len;
  char* ss;
/*
	ss = "\n  Время   	"; len = strlen (ss);							//  3 пробел 1таб. 
	for (k = 0; k<len; k++) { txt_pole[0][k] = *ss; ss++; }
	txt_pole_len[0] = len;

	ss = " Воздух   	"; len = strlen (ss);							// 3 пробел 1таб.
	for (k = 0; k<len; k++) { txt_pole[1][k] = *ss; ss++; }
	txt_pole_len[1] = len;

	ss = "Радиатор  	"; len = strlen (ss);				//  2 пробела 1таб.
	for (k = 0; k<len; k++) { txt_pole[2][k] = *ss; ss++; }
	txt_pole_len[2] = len; 

	ss = "  Задано  	"; len = strlen (ss);					// 3 пробела 1таб.
	for (k = 0; k<len; k++) { txt_pole[3][k] = *ss; ss++; }
	txt_pole_len[3] = len; 

	ss = "Откл.220B 	"; len = strlen (ss);		//  1таб. 
	for (k = 0; k<len; k++) { txt_pole[4][k] = *ss; ss++; }
	txt_pole_len[4] = len;

	ss = "Время нагр.\r\n"; len = strlen (ss);
	for (k = 0; k<len; k++) { txt_pole[5][k] = *ss; ss++; }
	txt_pole_len[5] = len;  
*/
  
	ss = "\n|Время"; len = strlen (ss);							//  
	for (k = 0; k<len; k++) { txt_pole[0][k] = *ss; ss++; }
	txt_pole_len[0] = len;

	ss = "|||||Возд."; len = strlen (ss);							//  
	for (k = 0; k<len; k++) { txt_pole[1][k] = *ss; ss++; }
	txt_pole_len[1] = len;

	ss = "||Радиат."; len = strlen (ss);				//  
	for (k = 0; k<len; k++) { txt_pole[2][k] = *ss; ss++; }
	txt_pole_len[2] = len; 

	ss = "|||Улица"; len = strlen (ss);					// 
	for (k = 0; k<len; k++) { txt_pole[3][k] = *ss; ss++; }
	txt_pole_len[3] = len; 

	ss = "||Откл.220B"; len = strlen (ss);		//   
	for (k = 0; k<len; k++) { txt_pole[4][k] = *ss; ss++; }
	txt_pole_len[4] = len;

	ss = "|||Нагр.\r\n"; len = strlen (ss); //  1 пробел
	for (k = 0; k<len; k++) { txt_pole[5][k] = *ss; ss++; }
	txt_pole_len[5] = len; 
}

void ready_rep(void) {
  uint8_t k, m = 0;
  uint8_t count_block = 0;
  uint32_t len_str = 0;
  uint8_t num_bl;
  uint8_t kor_space = 0;	
//char* sample_txt;
	init_constTxt();
/*
	rep_day[0][0] = 2.12; 	rep_day[0][1] = 45.2; 	rep_day[0][2] = 8.45; 	rep_day[0][3] = 3.74; 	rep_day[0][4] = 1.3; 			rep_day[0][5] = 2.01;
	rep_day[1][0] = 23.1; 	rep_day[1][1] = 43.22; rep_day[1][2] = 71.5; 		rep_day[1][3] = 23.13; 		rep_day[1][4] = 11.3; 	rep_day[1][5] = 33.12;
	rep_day[2][0] = 11.02; 	rep_day[2][1] = 5; 			rep_day[2][2] = 48.4; 	rep_day[2][3] = 63.12; 	rep_day[2][4] = 89.03; 		rep_day[2][5] = 32.08;
	rep_day[3][0] = 5.17; 	rep_day[3][1] = 63.26; 	rep_day[3][2] = 11.3; 	rep_day[3][3] = 88.65; 	rep_day[3][4] = 45.3; 		rep_day[3][5] = 33.07;
	rep_day[4][0] = 20.4; 	rep_day[4][1] = 40.72; 	rep_day[4][2] = -85.44; rep_day[4][3] = -9.87; 	rep_day[4][4] = 66.3; 		rep_day[4][5] = 55.5;
	rep_day[5][0] = 93.08; rep_day[5][1] = 70.92; 	rep_day[5][2] = 14.33; 	rep_day[5][3] = -73.99; 	rep_day[5][4] = 77.3; 	rep_day[5][5] = 23.2;
	
	rep_day[6][0] = 2.12; 	rep_day[6][1] = 45.2; 	rep_day[6][2] = 8.45; 	rep_day[6][3] = 3.74; 		rep_day[6][4] = 1.3; 		rep_day[6][5] = 2.01;
	rep_day[7][0] = 23.1; 	rep_day[7][1] = 45.22; rep_day[7][2] = 71.5; 		rep_day[7][3] = 23.13; 		rep_day[7][4] = 11.3; 	rep_day[7][5] = 33.12;
	rep_day[8][0] = 11.02; 	rep_day[8][1] = 5; 			rep_day[8][2] = 48.4; 	rep_day[8][3] = 63.12; 		rep_day[8][4] = 89.03; 	rep_day[8][5] = 32.08;
	rep_day[9][0] = 5.17; 	rep_day[9][1] = 63.26; 	rep_day[9][2] = 11.3; 	rep_day[9][3] = 88.65; 		rep_day[9][4] = 45.3; 	rep_day[9][5] = 33.07;
	rep_day[10][0] = 20.4; 	rep_day[10][1] = 40.72; rep_day[10][2] = -85.44;rep_day[10][3] = -9.87; 	rep_day[10][4] = 66.3; 	rep_day[10][5] = 55.5;
	rep_day[11][0] = 93.08;rep_day[11][1] = 70.92; rep_day[11][2] = 14.33;	rep_day[11][3] = -73.99;	rep_day[11][4] = 77.3; 	rep_day[11][5] = 23.2;
	
	rep_day[12][0] = 2.12; 	rep_day[12][1] = 45.2; 	rep_day[12][2] = 8.45; 	rep_day[12][3] = 3.74; 	rep_day[12][4] = 1.3; 			rep_day[12][5] = 2.01;
	rep_day[13][0] = 23.1; 	rep_day[13][1] = 43.22; rep_day[13][2] = 71.5; 		rep_day[13][3] = 23.13; 		rep_day[13][4] = 11.3; rep_day[13][5] = 33.12;
	rep_day[14][0] = 11.02; 	rep_day[14][1] = 5; 	rep_day[14][2] = 48.4; 	rep_day[14][3] = 63.12; 	rep_day[14][4] = 89.03; 	rep_day[14][5] = 32.08;
	rep_day[15][0] = 5.17; 	rep_day[15][1] = 63.26; rep_day[15][2] = 11.3; rep_day[15][3] = 88.65; rep_day[15][4] = 45.3; 		rep_day[15][5] = 33.07;
	rep_day[16][0] = 20.4; 	rep_day[16][1] = 40.72; rep_day[16][2] = -85.44; rep_day[16][3] = -9.87; 	rep_day[16][4] = 66.3; 	rep_day[16][5] = 55.5;
	rep_day[17][0] = 93.08; rep_day[17][1] = 70.92; rep_day[17][2] = 14.33; 	rep_day[17][3] = -73.99; rep_day[17][4] = 77.3; rep_day[17][5] = 23.2;
	
	rep_day[18][0] = 2.12; 	rep_day[18][1] = 45.2; 	rep_day[18][2] = 8.45; 	rep_day[18][3] = 3.74; 	rep_day[18][4] = 1.3; 	rep_day[18][5] = 2.01;
	rep_day[19][0] = 23.1; 	rep_day[19][1] = 45.22; rep_day[19][2] = 71.5; 	rep_day[19][3] = 23.13; rep_day[19][4] = 11.3; 	rep_day[19][5] = 33.12;
	rep_day[20][0] = 11.02; rep_day[20][1] = 5; 		rep_day[20][2] = 48.4; 	rep_day[20][3] = 63.12; rep_day[20][4] = 89.03; rep_day[20][5] = 32.08;
	rep_day[21][0] = 5.17; 	rep_day[21][1] = 63.26; 	rep_day[21][2] = 11.3; 	rep_day[21][3] = 88.65; rep_day[21][4] = 45.3; rep_day[21][5] = 33.07;
	rep_day[22][0] = 20.4; 	rep_day[22][1] = 40.72; rep_day[22][2] = -85.44; rep_day[22][3] = -9.87; 	rep_day[22][4] = 66.3; rep_day[22][5] = 55.5;
	rep_day[23][0] = 93.08; rep_day[23][1] = 70.92; rep_day[23][2] = 14.33;	rep_day[23][3] = -73.99;	rep_day[23][4] = 77.3; rep_day[23][5] = 23.2;
	*/
	/*
	for(k = 0; k < 24; k++){
		for(m = 0; m<6; m++){ rep_day[k][m] = 99.99; }
	}*/
/////////////////////////////////////////////////////////////////
//........... формируем первую строку///////////////
	for (k = 0; k<6; k++) {
		for (m = 0; m< txt_pole_len[k]; m++) {
				txt_mail[len_str] =  txt_pole[k][m]; len_str++;
		}
	}
	//////////////////////////////////////////
	for (num_bl = 0; num_bl<24; num_bl++) {
		for (count_block = 0; count_block<6; count_block++) {
			m = func_acs2 (rep_day[num_bl][count_block]); 
//			txt_mail[len_str] = ' '; len_str++;
			for (k = 0; k<m; k++) { 
				if (count_block == 1 || count_block == 2 || count_block == 3) { // если пишем температуру
					txt_mail[len_str] = acs2_symb[k]; len_str++; 
					if (k == (m-1)) { 																						// закидываем в массив код символов 'C	
						txt_mail[len_str] = '\''; len_str++; txt_mail[len_str] = 'C'; len_str++; txt_mail[len_str] = ' '; 
						len_str++; txt_mail[len_str] = ' '; len_str++;
						if ((acs2_symb[0] =='-' || acs2_symb[1]=='-') && (acs2_symb[4]!='.')) { txt_mail[len_str] = ' '; len_str++; } // для нормального отображения (выравнивания столбцов) в почте добавляем один пробел в случае отрицательного числа и если оно не 3 значное
					} 
					kor_space++;
				}	else { // если пишем время
					if (k == 0) { txt_mail[len_str] = ' ';}
					else { txt_mail[len_str] = acs2_symb[k]; }	
					len_str++; kor_space++;
						if (acs2_symb[k] == '.') { 
							txt_mail[len_str-1] = 0xd1; txt_mail[len_str] = 0x87; len_str++; // закидываем в массив код символа 'ч'
							txt_mail[len_str] = '.'; len_str++; 
//							txt_mail[len_str] = ' '; len_str++;
						}
						if (k == (m-1)) {
							txt_mail[len_str] = 0xd0; len_str++; txt_mail[len_str] = 0xbc; len_str++; // закидываем в массив код символа 'м'
//							txt_mail[len_str] = 0xd0; len_str++; txt_mail[len_str] = 0xb8; len_str++; // закидываем в массив код символа 'и'
//							txt_mail[len_str] = 0xd0; len_str++; txt_mail[len_str] = 0xbd; len_str++; // закидываем в массив код символа 'н'
							txt_mail[len_str] = '.'; len_str++; 
						}
				}
			}
/*			if(count_block == 1 || count_block == 2 || count_block == 3){
				kor_space = 12- kor_space;
			}
			else{ kor_space = 13- kor_space; }
			for(i = 0; i<kor_space; i++){ txt_mail[len_str] = '.'; len_str++; }			
			kor_space = 0;
*/
			txt_mail[len_str] = '	';	len_str++;		
		}
		txt_mail[len_str] = '\r'; len_str++; txt_mail[len_str] = '\n'; len_str++;
	}

	txt_mail[len_str] = '\0';
	send_MAIL ((char*)txt_mail, "Subject:Отчет за сутки", true);		//получается передать до 2 килобайт, если больше - вылетает в ошибку

}

