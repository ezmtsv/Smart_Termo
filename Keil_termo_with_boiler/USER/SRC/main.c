/* Includes ------------------------------------------------------------------*/
#include "main.h"

uint8_t Send_Buffer[64]; //??uint8_t Send_Buffer[135];//65
uint8_t PrevXferDone = 1;
uint8_t  corr_coeff[64];
uint8_t status_protect_flash = 0;
uint8_t flag_change_TMP = 0;
uint8_t flag_debag = 0;
//uint8_t count_try_sendWIFI = 0;
uint8_t count_success_answerTCP = 0;
uint8_t dim_for_SMART[192];
uint8_t buf_mail[144];
uint8_t data_OUT_COM[192];

int16_t delta_TMP;
uint16_t ADC_stm;
uint16_t change_tmp;
uint32_t hour_for_report;
uint16_t count_alarm_letter = 0;
uint16_t count_alarm_letterW = 0;
uint8_t data_from_soft[260];

uint16_t set_tempBoiler = 3000;         // 30.00 * 100
uint16_t set_gisttempBoiler = 400;      // 04.00 * 100
uint8_t flag_work_boiler = 0;           // 1 - работа с бойлером, 0 - только отопление
bool flag_st_heat_boiler = false;       // false - при работе с бойлером нагрев горячей воды выкл., true - нагрев горячей воды вкл.

uint32_t  damp[48];
uint32_t  *dp;

USB_CORE_HANDLE  USB_Device_dev;

int irq_but_usb =0;
int irq_buf = 0, ss, count_ = 0;
int flag_IRQ_RA0 = 0;
extern int seconds_start_server;

unsigned int time_save;

extern uint16_t buffer_ADC[200];
extern uint8_t Report_buf[64];


bool flag_cmdUSB;
bool UP_DW;
bool flag_HEAT_GAS = false;
bool comm_INDEC = false;
bool flag_COOL = false;
bool flag_read_buf_COM = false;
bool flag_sync_setTMP_tarif = false;
bool flag_now_DAY = true;
bool flag_paket_for_COM = false;
bool flag_answer_TCP = false;
bool flag_send_MAIL = false;
bool TIM2_period_edit = false;
bool flag_restartMC = false;

extern uint8_t status_heat_var;
extern uint8_t status_power220_var;
extern buffer_OUT_SMART out_for_SMART;
extern work_param PARAM;
extern uint8_t count_last_cmd;
extern int seconds_time;
extern uint32_t buf_COEFF[190];
extern unsigned int time_DAY;
extern unsigned int time_NIGHT;
extern com_ESP8266 flag_getESP8266;

extern uint16_t len_packet_COM;
extern bool flag_OK_comWIFI;
extern uint32_t	countIN_com;
extern unsigned char in_comPORT[300];
extern bool flag_initESP8266;
extern uint16_t count_start_dataWIFI, length_dataWIFI;
extern bool flag_make_end;
extern uint8_t number_connect_WIFI;
extern bool start_initESP;
extern bool exist_connect[10];
extern bool flag_start_sendWIFI;
extern bool com_sendOK;
extern bool end_timeout_getdata;
extern bool modul_ESP_connect;
extern bool flag_ready_RST_ESP;
extern uint8_t buf_data[64];
extern struct	param_ESP8266 init_param;
extern uint8_t len_nameSSID;
extern bool execute_new_net;
extern bool flag_ever_hour;
extern bool flag_alarm_A;
extern bool flag_alarm_W;
extern bool flag_alarm_HeaterOFF;

extern uint8_t mail_from[40];
extern uint8_t mail_from_len;
extern uint8_t mail_from64[32];
extern uint8_t mail_from_len64;
extern uint8_t mail_pass64[28];
extern uint8_t mail_pass_len64;
extern uint8_t mail_to[40];
extern uint8_t mail_to_len;
extern uint8_t mail_name_port[3];
extern uint8_t mail_name_serv[30];
extern uint8_t mail_name_serv_len;

extern uint8_t all_number_connect_WIFI;
extern uint16_t	stop_dog;
extern  Acc_ account_mail;
extern double rep_day[24][6];
extern double alarm_A;
extern double alarm_W;

extern double cur_charge;
bool charge_ON = false;
extern uint8_t acs2_symb[8];
bool no_accum = false;

extern uint8_t mode_disp;
//extern uint8_t set_mode;
/*
char* const temp_air_for_mail = "\r\rТемпература воздуха ";
char* const temp_water_for_mail = "\r\rТемпература теплоносителя ";
char* const temp_set_for_mail = "\r\rУстановленная температура ";
char* const heat_for_mail = "\r\rНагрев за период ";
char* const power_down_for_mail = "\r\rОтключение эл.сети ";
*/
uint8_t flag_report_mail_debug = 0;
uint32_t time_report_mail_debug = 25;

bool usb_status_count = false;

char* data_for_mail;

extern bool flag_req_IP;
/////////////////////////////////////////////////////
uint32_t CRCTable[256] = {
  0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 0x076DC419, 0x706AF48F, 0xE963A535, 0x9E6495A3,
  0x0EDB8832, 0x79DCB8A4, 0xE0D5E91E, 0x97D2D988, 0x09B64C2B, 0x7EB17CBD, 0xE7B82D07, 0x90BF1D91,
  0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE, 0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7,
  0x136C9856, 0x646BA8C0, 0xFD62F97A, 0x8A65C9EC, 0x14015C4F, 0x63066CD9, 0xFA0F3D63, 0x8D080DF5,
  0x3B6E20C8, 0x4C69105E, 0xD56041E4, 0xA2677172, 0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B,
  0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940, 0x32D86CE3, 0x45DF5C75, 0xDCD60DCF, 0xABD13D59,
  0x26D930AC, 0x51DE003A, 0xC8D75180, 0xBFD06116, 0x21B4F4B5, 0x56B3C423, 0xCFBA9599, 0xB8BDA50F,
  0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924, 0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D,
  0x76DC4190, 0x01DB7106, 0x98D220BC, 0xEFD5102A, 0x71B18589, 0x06B6B51F, 0x9FBFE4A5, 0xE8B8D433,
  0x7807C9A2, 0x0F00F934, 0x9609A88E, 0xE10E9818, 0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01,
  0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E, 0x6C0695ED, 0x1B01A57B, 0x8208F4C1, 0xF50FC457,
  0x65B0D9C6, 0x12B7E950, 0x8BBEB8EA, 0xFCB9887C, 0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3, 0xFBD44C65,
  0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2, 0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB,
  0x4369E96A, 0x346ED9FC, 0xAD678846, 0xDA60B8D0, 0x44042D73, 0x33031DE5, 0xAA0A4C5F, 0xDD0D7CC9,
  0x5005713C, 0x270241AA, 0xBE0B1010, 0xC90C2086, 0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,
  0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4, 0x59B33D17, 0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD,
  0xEDB88320, 0x9ABFB3B6, 0x03B6E20C, 0x74B1D29A, 0xEAD54739, 0x9DD277AF, 0x04DB2615, 0x73DC1683,
  0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8, 0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1,
  0xF00F9344, 0x8708A3D2, 0x1E01F268, 0x6906C2FE, 0xF762575D, 0x806567CB, 0x196C3671, 0x6E6B06E7,
  0xFED41B76, 0x89D32BE0, 0x10DA7A5A, 0x67DD4ACC, 0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5,
  0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252, 0xD1BB67F1, 0xA6BC5767, 0x3FB506DD, 0x48B2364B,
  0xD80D2BDA, 0xAF0A1B4C, 0x36034AF6, 0x41047A60, 0xDF60EFC3, 0xA867DF55, 0x316E8EEF, 0x4669BE79,
  0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236, 0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F,
  0xC5BA3BBE, 0xB2BD0B28, 0x2BB45A92, 0x5CB36A04, 0xC2D7FFA7, 0xB5D0CF31, 0x2CD99E8B, 0x5BDEAE1D,
  0x9B64C2B0, 0xEC63F226, 0x756AA39C, 0x026D930A, 0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713,
  0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38, 0x92D28E9B, 0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21,
  0x86D3D2D4, 0xF1D4E242, 0x68DDB3F8, 0x1FDA836E, 0x81BE16CD, 0xF6B9265B, 0x6FB077E1, 0x18B74777,
  0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C, 0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45,
  0xA00AE278, 0xD70DD2EE, 0x4E048354, 0x3903B3C2, 0xA7672661, 0xD06016F7, 0x4969474D, 0x3E6E77DB,
  0xAED16A4A, 0xD9D65ADC, 0x40DF0B66, 0x37D83BF0, 0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,
  0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6, 0xBAD03605, 0xCDD70693, 0x54DE5729, 0x23D967BF,
  0xB3667A2E, 0xC4614AB8, 0x5D681B02, 0x2A6F2B94, 0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B, 0x2D02EF8D,

};
    /////////////////////////////////////////////////////////////

__IO uint32_t VectorTable[48] __attribute__((at(0x20000000)));
/* Private function prototypes -----------------------------------------------*/

/**
  * @brief  Main program.
  * @param  None
  * @retval None
  */
void func_get_data_TCP (uint16_t start_b);	
void data_answer (void);	
void data_transmit_clientWIFI (void);
uint32_t CalculateCRC (uint8_t dim_crc[], uint32_t size);
void func_synchro (void);	
void func_heat (uint8_t GAS, bool flg_cool, bool heat);		
void ready_str_for_alarm (char* txt, char* sub);		
void load_DEF_setESP (void);



void Delay (int nTime) {
	double dell;
	dell = (double)(nTime)*6.7;
  nTime = (int)dell;
  while (nTime != 0) nTime--;;
}



void DelayuS(__IO uint32_t nCount) {
  __IO uint32_t aa;
  __IO uint32_t nnnCount = 1000;
  for (aa = 0; aa < nCount ; nCount--) {
    for (; nnnCount!= 0; nnnCount--) ;
  };
}



void DelaymS(__IO uint32_t nCount) {
	__IO uint32_t aa;
  for (aa = 0; aa < nCount ; nCount--) {
		DelayuS (1000);
	}
}



void led_answer (void) {
	for (ss = 0; ss<4; ss++) {
		led_one_ON (LED_G);
		DelaymS (80);
		led_one_OFF (LED_G);
		DelaymS (80);
	}	
}



uint32_t flash_read (uint32_t address) {
  return (*(__IO uint32_t*) address);
}



void reset_ESP8266 (void) {
	RST_ESP8266_ON;
	DelaymS (500);
	RST_ESP8266_OFF;
	DelaymS (1500);
}



extern const unsigned char seg_data[10];


__STATIC_INLINE void NVIC_SystemReset (void);

int main (void) {
  
	uint32_t i = 0, j = 0;
	double tmpdouble_var;
	uint32_t tmpint_var;
//	uint8_t buf[40];
//	char* net_ap;
//	char* pas_ap;
	//////////////////////////////
/*		const char* first = "first"; // 
    const char* second = "second";
    char* buff = calloc(strlen(first) + strlen(second) + 1, 1);
    strcat(buff, first);
    strcat(buff, second);
    printf("%s\n", buff);
    free(buff);*/
	///////////////////////////////
	
  for(i = 0; i < 48; i++) {
    VectorTable[i] = *(__IO uint32_t*)(MAIN_PROGRAM_START_ADDRESS + (i<<2));
  }
  /* Enable the SYSCFG peripheral clock*/
  RCC_APB2PeriphResetCmd (RCC_APB2Periph_SYSCFG, ENABLE); 
  /* Remap SRAM at 0x00000000 */
  SYSCFG_MemoryRemapConfig (SYSCFG_MemoryRemap_SRAM);
  /* Initialize USB  */
  USBD_Init(&USB_Device_dev,
            &USR_desc, 
            &USBD_HID_cb, 
            &USR_cb);
  
	init_gpio_();
	/*
	InitializeLCD(); //Инициализация дисплея 1602
	LCM_OUT_portA &= (~LCM_PIN_EN);
	*/
	///////////////////////////////////
  init_tmr();
  init_tmr2();
  iwdg_init();
	
  led_one_OFF (LED_G);
  led_one_OFF (LED_R);
  TIM_Cmd (TIM3, ENABLE);
  TIM_Cmd (TIM2, ENABLE);
//	status_busy = status_NO;
	out_for_SMART.status_BUSY = status_NO;
	init_ADC_STM ();
	//RELE_Termo_ON;
// на демо плате из соло - белый TX, зеленый RX, черный GND
	comport_Init (COM2, 115200);
//		reset_ESP8266();
  for (ss = 0; ss<3; ss++) {
    led_one_ON (LED_R);
    DelaymS (80+ss*20);
    led_one_OFF (LED_R);
    DelaymS (80+ss*20);
    led_one_ON (LED_R);
    DelaymS (80+ss*20);
    led_one_OFF (LED_R);
    DelaymS (80+ss*20);
  }
  for (ss = 0; ss<3; ss++) {
    led_one_ON (LED_G);
    DelaymS (80+ss*20);
    led_one_OFF (LED_G);
    DelaymS (80+ss*20);
    led_one_ON (LED_G);
    DelaymS (80+ss*20);
    led_one_OFF (LED_G);
    DelaymS (80+ss*20);
  }
  if (status_STATUS_220) {						// проверка, выставлен ли бит ADC_IN9
//			status_power220_var = status_NO;
    out_for_SMART.status_power220 = status_NO;
  }	else {
//			status_power220_var = status_OK;
    out_for_SMART.status_power220 = status_OK;
  }
  read_FLASH_CORR();
  read_modework();
//		init_WIFI_server();
	while (!modul_ESP_connect) { 
    reset_ESP8266(); 
    init_WIFI_server(); 
    i++; 
    if (i>2) break; 
  };
	DelaymS (1000); 
  reset_ESP8266(); 
  init_WIFI_server();
  DelaymS (1000);
	read_modework();
///////////////////////////////////////	
//		data_for_mail = "\r\rТемпература воздуха ";
//		data_for_mail = temp_water_for_mail;
//////////////////////////////////////////
  PARAM.SECOND_ = seconds_time;			// для синхронизации после рестарта в ноль часов
  func_synchro();
/////////////////////////////////////////		
	for (i = 0; i < 24; i++) {
    for (j = 0; j<6; j++) rep_day[i][j] = 99.99;
  }
//		data_for_mail = data_for_mail+temp_air_for_mail;
/////////////debug////////////////////////////////
//		out_for_SMART.work_TARIF = status_OK;
//////////////////////////////////////////////////		
	
	////////////debug///////////
/*
	InitializeLCD(); //Инициализация дисплея 1602
	ClearLCDScreen(); // очистка памяти дисплея
	Cursor(0,4); //Установка курсора, 0-ая строка, 0-ой столбец
	PrintStr("AIR +25.02"); 
	Cursor(1,2);
	PrintStr("Water +65.87"); 
	*/
	/////////////debug init TM1637/////////
	TM1637_init(); TM1637_clear(); i = 0;
  init_tmr16 ();
//  GREEN_DISP_ON;
//  RED_DISP_ON;
	////////////////////////////
  while (1) {

		start_cycle:		
		/////////////////////// обработка сообщения от смарта
		if (flag_answer_TCP) {
			switch (out_for_SMART.COMAND) {
/*
				case inc_tmp: 
					out_for_SMART.set_TMP = out_for_SMART.set_TMP+change_tmp;
					save_modework (save_setTMP, out_for_SMART.set_TMP, 0);

				break;
				case dec_tmp: 
					out_for_SMART.set_TMP = out_for_SMART.set_TMP - change_tmp;
					save_modework (save_setTMP, out_for_SMART.set_TMP, 0);

				break;	
*/
        case set_tmp:
					out_for_SMART.set_TMP = change_tmp;
					save_modework (save_setTMP, out_for_SMART.set_TMP, 0);  
        break;
				case synchro:
					seconds_time = PARAM.SECOND_;
////////////////////////////////		out_for_SMART.count_off_POW = 0;				out_for_SMART.count_work_HEAT = 0; seconds_start_server
					if (seconds_time<seconds_start_server) seconds_start_server = seconds_time;
					tmpint_var = seconds_time / 60;
					if (tmpint_var < out_for_SMART.count_off_POW) out_for_SMART.count_off_POW = tmpint_var;
					if (tmpint_var < out_for_SMART.count_work_HEAT) out_for_SMART.count_work_HEAT = tmpint_var;
					func_synchro();
					flag_answer_TCP = false;															// после получения любой команды, кроме set_link, заходим и отрабатываем case synchro:, потом сбрасываем flag_answer_TCP
/////////////////////////////////////////////		seconds_time > time_NIGHT && seconds_time < time_DAY					
				break;
				case set_GAS:
					out_for_SMART.status_mode_GAS = PARAM.work_GAS;
					out_for_SMART.gisteresis_TMP = PARAM.gisteresis_TMP_;
					i = (uint32_t)out_for_SMART.status_mode_GAS | (uint32_t)(out_for_SMART.gisteresis_TMP<<8);
					save_modework (save_work_modeGAS, i, 0);
					if (out_for_SMART.status_mode_GAS == status_NO) RELE1_OFF;
					if (out_for_SMART.status_mode_GAS == status_OK) RELE2_OFF;
  			break;
				case set_work_TARIF:
//					seconds_time = PARAM.SECOND_;
///////////////////////////////////////////////////
					out_for_SMART.work_TARIF = PARAM.work_TARIF_;
					out_for_SMART.gisteresis_TARIF = PARAM.gisteresis_TARIF_;
					i = (uint32_t)out_for_SMART.work_TARIF | (uint32_t)(out_for_SMART.gisteresis_TARIF<<8);
					save_modework (save_work_modeTARIF, i, 0);		
///////////////////////////////////////////////////				
				break;
				case cmd_save_kf:
					save_coeff_cor (dim_for_SMART);
				break;
				case synchro_timeTARIF:
					time_NIGHT = PARAM.time_NIGHT_;
					time_DAY = PARAM.time_DAY_;
//					seconds_time = PARAM.SECOND_;
					save_modework (save_time_modeTARIF, time_NIGHT, time_DAY);
				break;
				case cool_modeON:
					flag_COOL = true;
					save_modework (mode_cool_heat, cool_modeON, 0);
				break;
				case cool_modeOFF:
					flag_COOL = false;
					save_modework (mode_cool_heat, cool_modeOFF, 0);
				break;
				case set_link:
					set_newNET (dim_for_SMART);
					flag_answer_TCP = false;
					flag_restartMC = true;										// для принудительного рестарта через 2мин после сохпанения настроек новой сети
//					save_modework(mode_cool_heat, cool_modeOFF, 0);
				break;
				case config_mail:
					for (i = 0; i<52; i++) buf_mail[i] = dim_for_SMART[i+4];
					for (i = 0; i<90; i++) buf_mail[i+52] = dim_for_SMART[i+69];
					save_net (buf_mail, config_mail, 4*36);
					DelaymS (150);
					read_modework();
				break;
				case load_def:
					load_DEF_setESP();
				break;
        case set_gisttmpBR:  
          set_gisttempBoiler = (uint16_t)(PARAM.gistTMPBR_)*100;
        break;
        case set_tmpBR:
          set_tempBoiler = PARAM.TMPBR_;
        break;
        case set_ONOFFboiler:
          flag_work_boiler = PARAM.flagBR_;
        break;
        case setFlagAlarmONOF:
          if (PARAM.flag_AlarmOFF == 1) flag_alarm_HeaterOFF = true;
          else flag_alarm_HeaterOFF = false;
        break;
			}
			if (out_for_SMART.COMAND != set_link) out_for_SMART.COMAND = synchro;
//			flag_answer_TCP = false;	
		}	
//===================================
//===================================    
		if (flag_change_TMP!=0) {									// флаг изменения температуры при работе по тарифу, выставляется в прерываниях
			switch (flag_change_TMP) {
				case 1:
//					func_change_tmp(false, out_for_SMART.gisteresis_TARIF);
				break;
				case 2:
//					func_change_tmp(true, out_for_SMART.gisteresis_TARIF);
				break;
			}
			flag_change_TMP = 0;
		}
//===================================
//===================================    
///////////////////////определение статусов нагрева и наличия эл. питания//////////////////////////		
		if (status_STATUS_220) {						// проверка, выставлен ли бит ADC_IN9
//			status_power220_var = status_NO;
			out_for_SMART.status_power220 = status_NO;
		}				
		else{
//			status_power220_var = status_OK;
			out_for_SMART.status_power220 = status_OK;
		}
//===================================
/*    
    if (set_tempBoiler > out_for_SMART.tmp_B && flag_work_boiler != 0) {  // переключение на бойлер (при старте модуля бойлер по умолчанию всегда выключен - flag_work_boiler = 0)
      RELE3_ON;
      GREEN_DISP_OFF;
      RED_DISP_ON;
      flag_st_heat_boiler = true;                                                         // включился нагрев воды 
      if (!flag_alarm_W) func_heat (out_for_SMART.status_mode_GAS, true, true);				    // включить нагрев
      else {
        get_strBUF_USB ("ALARM Boiler! Overheating water!"); 
        func_heat (out_for_SMART.status_mode_GAS, true, false);								            // выключить нагрев
      }
    } else {
      if ((set_tempBoiler + set_gisttempBoiler) < out_for_SMART.tmp_B && flag_work_boiler !=0) { 
        RELE3_OFF;
        RED_DISP_OFF;
        GREEN_DISP_ON;
        func_heat (out_for_SMART.status_mode_GAS, true, false);								            // выключить нагрев
        flag_st_heat_boiler = false;                                                      // нагрев воды выключился
      } else {                                                            // переключение на отопление
        if (flag_work_boiler == 0){
          RED_DISP_OFF;
          GREEN_DISP_OFF;        
        }
//===================================    
          delta_TMP = ((int16_t)out_for_SMART.set_TMP)*100 - out_for_SMART.tmp_AIR;		    // находим разницу между реальной температурой возд. и установленной
          if (!flag_alarm_W) {																														// если темп. воды не превышает критическую 95гр.цельс.
            if (delta_TMP>0) {
              func_heat (out_for_SMART.status_mode_GAS, flag_COOL, true);								  // включить нагрев
            } else {
              func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);								// выключить нагрев
            }
          } else { 																																				// температура воды выше критической  или принудительное аварийное откл., выключить нагрев
            func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false); 
            get_strBUF_USB ("ALARM! Overheating water!"); 
          }
//===================================      
      }
    }
*/
  if (flag_work_boiler) {
    if (set_tempBoiler > out_for_SMART.tmp_Boiler) {                                                          // переключение на бойлер (при старте модуля бойлер по умолчанию всегда выключен - flag_work_boiler = 0)  
      RELE3_ON;
      GREEN_DISP_OFF;
      RED_DISP_ON;
      flag_st_heat_boiler = true;                                                                             // включился нагрев воды 
      if (!flag_alarm_W && !flag_alarm_HeaterOFF) func_heat (out_for_SMART.status_mode_GAS, flag_COOL, true);	// включить нагрев
      else {
        if (!flag_alarm_HeaterOFF) get_strBUF_USB ("ALARM Boiler! Overheating water!");                       // температура воды выше критической  или принудительное аварийное откл., выключить нагрев
        else get_strBUF_USB ("ALARM Heater OFF enabled!");
        func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);								                          // выключить нагрев
      }    
    } else {
//      if ((set_tempBoiler + set_gisttempBoiler) < out_for_SMART.tmp_Boiler) { 
      if ((out_for_SMART.tmp_Boiler-set_tempBoiler)>set_gisttempBoiler) {  
        RELE3_OFF;
        RED_DISP_OFF;
        GREEN_DISP_ON;
        func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);								 // выключить нагрев
        flag_st_heat_boiler = false;                                                 // нагрев воды выключился
      }    
    }
  } else {
    RED_DISP_OFF;
    GREEN_DISP_OFF;  
    flag_st_heat_boiler = false;
    RELE3_OFF;
  }
  if (!flag_st_heat_boiler) {                                                       // если в данный момент не идет нагрев воды 
    delta_TMP = ((int16_t)out_for_SMART.set_TMP)*100 - out_for_SMART.tmp_AIR;		    // находим разницу между реальной температурой возд. и установленной
    if (!flag_alarm_W && !flag_alarm_HeaterOFF) {																														// если темп. воды не превышает критическую 95гр.цельс.
      if ((uint16_t)out_for_SMART.set_TMP*100 > out_for_SMART.tmp_AIR)
        func_heat (out_for_SMART.status_mode_GAS, flag_COOL, true);								  // включить нагрев
      else {
        if (((uint16_t)out_for_SMART.set_TMP*100 + out_for_SMART.gisteresis_TMP) < out_for_SMART.tmp_AIR)
          func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);							// выключить нагрев
      }
      } else { 																																				// температура воды выше критической или принудительное аварийное откл., выключить нагрев
        if (!flag_alarm_HeaterOFF) get_strBUF_USB ("ALARM! Overheating water!_"); 
        else get_strBUF_USB ("ALARM Heater OFF enabled!_");
      func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false); 
    }  
  }
//===================================      
/////////////////////////работа с СОМ портом //////////////////		
//////////////////////////////ESP8266	    
		switch (flag_getESP8266) {  
			case com_status:
				;
			break;
			case com_get_data:				// прин начале приема данных попадаем сюда, выжидаем 250мс, чтобы получить все данные
				flag_getESP8266 = com_get_connect;
				DelaymS (250);						
//				number_connect_WIFI = in_comPORT[7] - 48;  	// 48 - код нуля
////////////////////////////////////////////////////
				length_dataWIFI = size_get_bytes_from_com (count_start_dataWIFI);		                            // после выполнения модифицируется count_start_dataWIFI, в зависимости от числа принятых байт
				for (ss = 0; ss<192; ss++) dim_for_SMART[ss] = (uint8_t)in_comPORT[ss+count_start_dataWIFI];		// переписываем полученные данные в массив dim_for_SMART
				if (in_comPORT[count_start_dataWIFI] == 'E' && in_comPORT[count_start_dataWIFI+1] == 'Z' &&
					in_comPORT[count_start_dataWIFI+2] == 'A' && in_comPORT[count_start_dataWIFI+3] == 'P') func_get_data_TCP(count_start_dataWIFI);
			
				send_str_USB ("length_dataWIFI :", (uint8_t)length_dataWIFI);
				DelaymS (80);
//				send_str_USB ("Data recieved! ", count_start_dataWIFI);
//				DelaymS (80);
				sendUSB_getCOM_data (length_dataWIFI, count_start_dataWIFI);
			break;
			case com_get_busy:
				if (!flag_make_end) {
					DelaymS (100);
          get_strBUF_USB ("Module WIFI is busy!"); 
          DelaymS (100);
				}
//				flag_getESP8266 = com_no;
				flag_make_end = true;
			break;
			case com_get_connect:
				//send_strUSB("Connect WIFI :"); 
				if (!flag_make_end) send_str_USB ("ESP connect WIFI :", number_connect_WIFI);
//				flag_getESP8266 = com_no;
				flag_make_end = true;
			break;
			case com_get_closed:
				//send_strUSB("Disconnect WIFI :"); flag_getESP8266 = com_no;
				if (!flag_make_end) send_str_USB ("ESP disconnect WIFI :", number_connect_WIFI);
//				flag_getESP8266 = com_no;
				flag_make_end = true;
			break;
			case com_get_link_not_valid:									// 
				send_str_USB ("Connection is lost! ", number_connect_WIFI);
				exist_connect[number_connect_WIFI] = false;
				flag_getESP8266 = com_no;
				modul_ESP_connect = true;   // при передачи данных был сбой, чтобы избежать перезагрузки через 2 мин выставляем флаг modul_ESP_connect
			break;
			case com_get_error:
				;
			break;
			case com_no:
				;
			break;
		}
//===================================
//===================================      
		if (flag_answer_TCP) goto start_cycle; // получена команда по TCP, переход на обработку данных от смарта
		
		if (start_initESP) {
			start_initESP = false;
			SerialPutString ("AT+RST\r\n");
			DelaymS (2000);
			init_WIFI_server();
		}
		if (flag_paket_for_COM) {							/// запись данных принятых по USB в ком порт через утилиту
			for (i = 0; i<len_packet_COM; i++) {
				SerialPutChar (data_from_soft[i]);
			}
			SerialPutString ("\r\n");
			flag_paket_for_COM = false;
		}
		
		if (flag_start_sendWIFI) {															// по этому флагу передаются данные клиенту WIFI
////////////////////////////////////////////
				work_ADC_STM();
				real_temp();
				out_for_SMART.SECOND = seconds_time;
				init_out_for_SMART (&out_for_SMART);
				data_transmit_clientWIFI();			// для работы с модулем WIFI
//				out_temp_LCDTM1637 (alarm_A);
///////////////////////////////////////////			
				if (flag_getESP8266 == com_get_connect) { 
//					ready_send_dataWIFI (number_connect_WIFI, len_packet_COM, data_OUT_COM); 	//count_try_sendWIFI = 0; 
					if (all_number_connect_WIFI>9) all_number_connect_WIFI = 0;
					for (i = 0; i < all_number_connect_WIFI; i++) {
						ready_send_dataWIFI (i, len_packet_COM, data_OUT_COM); 	 
					}
//					led_one_OFF(LED_R);
				}	else { 
//					get_strBUF_USB("Transmission is not possible, connection fail!"); 
//					send_str_USB("Transmission is not possible, flag_getESP8266 =", flag_getESP8266);
	//////////////////////DEBUG INFO transmit to USB port//////////////////////////////////				
//					send_str_USB("Lenght name SSID WIFI = ", len_nameSSID); 
//					send_str_USB("mail_pass_len = ", mail_pass_len64);
//					send_str_USB("mail_account = ", account);buf_mail
//					send_str_USB("buf_mail[115] = ", buf_mail[115]);
//					DelaymS(100);
					///////////////////////
//					mail_pass64[0] = 'D'; mail_pass64[1] = 'e'; mail_pass64[2] = 'b'; mail_pass64[2] = 'a'; mail_pass64[3] = 'g'; 
					///////////////////////
//					get_strBUF_USB(init_param.pass_wifi); DelaymS(200);
//					get_strBUF_USB(init_param.SSID_wifi); 
//					get_strBUF_USB(mail_pass64);
//					get_strBUF_USB(mail_from);
///debug
					get_strBUF_USB ("not link with device");
          //send_str_USB ("mode_disp ", mode_disp);
					DelaymS (200);
          //send_str_USB ("set_mode ", set_mode);
          send_str_USB ("charge_ Accum ", (uint8_t)(cur_charge*10));
///          
					DelaymS (1000); //count_try_sendWIFI++; 
				}
//				if(count_try_sendWIFI > 7){ flag_start_sendWIFI = false; count_try_sendWIFI = 0; }
		}
		if (com_sendOK) {	
//			get_strBUF_USB("Data success transmit!"); // при успешной передачи данных
			send_str_USB ("Data success transmit! ", all_number_connect_WIFI);
			if (out_for_SMART.COMAND != cmd_req) { 
				count_success_answerTCP++;
				if (count_success_answerTCP>4){ 
          count_success_answerTCP = 0; 
          out_for_SMART.COMAND = cmd_req; 
          buf_data[2] = 88;}                // 5 ответов дублируем полученную команду, потом сбрасываем её 
			}
			com_sendOK = false; 
		}  
//		if(flag_OK_comWIFI && !flag_initESP8266){
		///////////////////////////// реализация передачи по USB принятых от ком данных до 400 байт		
		if (end_timeout_getdata) {							// вывод данных принятых от ком в USB порт 
			out_echo_from_ESP();
		}
		/////////////////////////////// чтение 228 байт данных из буфера ком, для дебагера		
		if (flag_read_buf_COM) {
      
				for (j = 0; j < 4; j++) {
          for (i = 0; i<57; i++) Send_Buffer[i+7] = in_comPORT[i+j*57];
					Send_Buffer[4] = 0x74; Send_Buffer[6] = 57;
//					Send_Buffer[7] = 0x99;	Send_Buffer[8] = 0x99;	Send_Buffer[9] = 0x99;	
					data_answer();	DelaymS(100);
				}
			flag_read_buf_COM = false; 
/// debug for mail ///
/*
      Send_Buffer[7] = '\n';
      Send_Buffer[4] = 0x74; Send_Buffer[6] = 1;
      data_answer();	DelaymS(100);
      
      uint32_t len_val;
      if (mail_from_len < 40) len_val = mail_from_len;
      else len_val = 40;
      for (i = 0; i<len_val; i++) Send_Buffer[i+7] = mail_from[i];
      Send_Buffer[7+len_val] = '\n';
			Send_Buffer[4] = 0x74; Send_Buffer[6] = len_val+1;
			data_answer();	DelaymS(100);

      if (mail_to_len < 40) len_val = mail_to_len;
      else len_val = 40;
      for (i = 0; i<40; i++) Send_Buffer[i+7] = mail_to[i];
      Send_Buffer[7+len_val] = '\n';
			Send_Buffer[4] = 0x74; Send_Buffer[6] = len_val+1;
			data_answer();	DelaymS(100);

      if (mail_from_len64 < 32) len_val = mail_from_len64;
      else len_val = 32;
      for (i = 0; i<len_val; i++) Send_Buffer[i+7] = mail_from64[i];
      Send_Buffer[7+len_val] = '\n';
			Send_Buffer[4] = 0x74; Send_Buffer[6] = len_val+1;
			data_answer();	DelaymS(100);

      if (mail_pass_len64 < 28) len_val = mail_pass_len64;
      else len_val = 28;
      for (i = 0; i<len_val; i++) Send_Buffer[i+7] = mail_pass64[i];
      Send_Buffer[7+len_val] = '\n';
			Send_Buffer[4] = 0x74; Send_Buffer[6] = len_val+1;
			data_answer();	DelaymS(100);

      for (i = 0; i<3; i++) Send_Buffer[i+7] = mail_name_port[i];
      Send_Buffer[7+3] = '\n';
			Send_Buffer[4] = 0x74; Send_Buffer[6] = 3+1;
			data_answer();	DelaymS(100);
      
      if (mail_name_serv_len < 28) len_val = mail_name_serv_len;
      else len_val = 30;
      for (i = 0; i<len_val; i++) Send_Buffer[i+7] = mail_name_serv[i];
      Send_Buffer[7+len_val] = '\n';
			Send_Buffer[4] = 0x74; Send_Buffer[6] = len_val+1;
			data_answer();	DelaymS(100);     

			send_str_USB("mail_from_len = ", mail_from_len); DelaymS(100);
			send_str_USB("mail_from_len64 = ", mail_from_len64); DelaymS(100);
			send_str_USB("mail_pass_len64 = ", mail_pass_len64); DelaymS(100);
			send_str_USB("mail_to_len = ", mail_to_len); DelaymS(100);
      send_str_USB("mail_name_serv_len = ", mail_name_serv_len); DelaymS(100);
      send_str_USB("account_mail = ", account_mail); DelaymS(100);    
      
			flag_read_buf_COM = false;
      */
/// debug for mail ///      
			led_answer();
		}
		if (flag_ready_RST_ESP) {						// переключаем модуль в режим точки доступа для введения параметров новой сети
			load_DEF_setESP();
		}
		if (flag_send_MAIL) {
//			send_MAIL("This message sent from ESP8266! This message sent from ESP8266! Это сообщение получено от ESP8266!", "Subject:test send MAIL!!!");
			if(account_mail !=0) { ready_rep(); }
//			ready_str_for_alarm("тест!", "Subject:SET MODE!");		// for debug
			flag_send_MAIL = false;
			led_one_OFF (LED_R);
		}
//////////////////////////////ESP8266		
//////////////////////заполняем массив для отчетов	
		if (flag_ever_hour) {
			hour_for_report = seconds_time / 3600;
			tmpdouble_var = (double)(hour_for_report)+0.45;																										// преобразуем текущее время к формату - целая часть часы, дробная минуты
			rep_day[hour_for_report][0] = tmpdouble_var;
			rep_day[hour_for_report][1] = ((double)out_for_SMART.tmp_AIR)/100;																// сохраняем темп. воздуха
			if(out_for_SMART.znaki_TMP & 1){ rep_day[hour_for_report][1] = rep_day[hour_for_report][1] *-1;}	// если минус, то меняем знак
			rep_day[hour_for_report][2] = ((double)out_for_SMART.tmp_WATER)/100;															// сохраняем темп. воды
			if(out_for_SMART.znaki_TMP & 2){ rep_day[hour_for_report][2] = rep_day[hour_for_report][2] *-1;}	// если минус, то меняем знак
//			rep_day[hour_for_report][3] = (double)out_for_SMART.set_TMP;																			// сохраняем установленную темп. 
      rep_day[hour_for_report][3] = (double)out_for_SMART.tmp_Outdoor/100;
      if(out_for_SMART.znaki_TMP & 4){ rep_day[hour_for_report][3] = rep_day[hour_for_report][3] *-1;}	// если минус, то меняем знак
      
			tmpint_var = out_for_SMART.count_off_POW % 60;
			tmpdouble_var = ((double)tmpint_var)/100+ (double)(out_for_SMART.count_off_POW / 60);
			rep_day[hour_for_report][4] = tmpdouble_var;
			tmpint_var = out_for_SMART.count_work_HEAT % 60;
			tmpdouble_var = ((double)tmpint_var)/100+ (double)(out_for_SMART.count_work_HEAT / 60);			
			rep_day[hour_for_report][5] = tmpdouble_var;
			flag_ever_hour = false;
		}
///////////////////события по тревоге(перегрев или падение температуры ниже уст. минимума)		
    if (account_mail != 0) {														// условие, что почтовые параметры были сохранены  // нужно добавить флаг регистрации в сети flag_gotIP
      if (flag_alarm_A ) {
        if (count_alarm_letter < 10) {
//				ready_str_for_alarm ("Alarm mode, temperature air fail down more than -3 degrees Celsius!!!", "Alarm WATER!");
          ready_str_for_alarm ("Аварийный режим, температура воздуха опустилась ниже 3˚С!!!", "Subject:Alarm AIR!");
          count_alarm_letter = 10;
        }
        count_alarm_letter++; 
        if (count_alarm_letter>400) count_alarm_letter = 0;
        flag_alarm_A = false;
      }	else count_alarm_letter = 0;
      if (flag_alarm_W ) {
        if (count_alarm_letterW < 10) {
//			  ready_str_for_alarm("Alarm mode, increase temperature water more than 95 degrees Celsius!!!", "Alarm WATER!");
          ready_str_for_alarm ("Аварийный режим, температура теплоносителя поднялась выше 95˚С!!!", "Subject:Alarm WATER!");
          count_alarm_letterW = 10;
        }
        count_alarm_letterW++; 
        if (count_alarm_letterW>400) count_alarm_letterW = 0;
        flag_alarm_W = false;
      }	else count_alarm_letterW = 0;	
    }
/////////////////////////////////////////////
// при старте, в случае отключенного аккум., выставит бит charge_ON и когда подключим аккум будет выполнена зарядка, если напряжение аккум меньше
// 4,1В. Если аккум подключен, то при старте бит charge_ON останется сброшеным, и зарядка пойдет только при падении напряжения на аккум меньше 3,7В
		if (charge_ON) {														/// зарядка вкл.
			if (cur_charge > 4.18) {										/// напряжение на аккум. превысило порог 4.18В
				pinCHARGE_OFF;
				charge_ON = false;
			}
		} else {																		/// зарядка выкл.	
			if (cur_charge < 3.7) {										/// напряжение на аккум. упало ниже 3.7В 
					pinCHARGE_ON;
					charge_ON = true;	
			}				
		}	
		if (cur_charge > 3.1) no_accum = false; 	///		/// условие наличия аккум.
		else { no_accum = true; pinCHARGE_OFF; }			/// без аккум. будет мигать
//		send_str_USB("charge_ Accum ", (uint8_t)(cur_charge*10));
/////////////////////////////////////////////
//		stop_dog = 0;		////__disable_irq(); while(1){;}  ///для дебага
		if (!flag_restartMC) stop_dog = 0;		// для принудительного рестарта через 2мин после сохпанения настроек новой сети, stop_dog не будет сбрасываться после сохранения новой сети

////////////debug for displey 1602//////////////////
/*
DelaymS(1500);
send_str_USB("work main cycle", 0);
out_temp_LCD(-25.47,83.32);
DelaymS(1500);
out_alarm_LCD("Not Power!");
*/
////////////debug for displey TM1637//////////////////
/*	DelaymS(1500);
	send_str_USB("work main cycle ", i);
	if(i>9)i = 0;
	alarm_A = -525.658 + (double)i;
	out_temp_LCDTM1637(alarm_A); i++;*/
///////////////////////////////////////////////////
		switch (USB_Device_dev.dev.device_status) {
			case USB_CONFIGURED:
				if (usb_status_count) { 
					usb_status_count = false;    
					USBD_Init (&USB_Device_dev, &USR_desc, &USBD_HID_cb, &USR_cb);
					send_str_USB ("USB_status CONFIGURED ", 0);
				}
				
			break;
			case USB_SUSPENDED:
				usb_status_count = true;
//				send_str_USB("USB_status SUSPENDED ", 0);
			break;
			case USB_UNCONNECTED:
				usb_status_count = true;
				send_str_USB ("USB_status UNCONNECTED ", 0);
			break;
			case USB_DEFAULT:
				send_str_USB ("DEFAULT ", 0);
			break;
			case USB_ADDRESSED:
				send_str_USB ("ADDRESSED ", 0);
			break;
		}
/////////////////////////////////////////////		
  }  
/////////////////////////////////////////////		END while (1)		
}
/////////////////////////////////////////////		END main
///////////////////////////////////////////////////////////
void load_DEF_setESP (void) {
	
			__disable_irq();	
			DelaymS (900);
			FLASH_Unlock();
			FLASH_ErasePage (ADDRESS_KORR_COEFF);
			FLASH_Lock();
			__enable_irq();
			NVIC_SystemReset();
	
}
///////////////////////////////////////////////////////////
void ready_str_for_alarm (char* txt, char* sub) {
//	char* str ="";
	char dim_txt[350];
	char* txt_for_air = "\n Текущая температура воздуха ";
	char* txt_for_water = "\n Текущая температура теплоносителя ";
//			char* txt_for_air = "\n Current temperature air ";
//			char* txt_for_water = "\n Current temperature water ";
	size_t len_a = strlen (txt_for_air);
	size_t len_w = strlen (txt_for_water);
	size_t len = strlen (txt);
	uint16_t t, m, k = 0;
	
	for (t = 0; t<len; t++){	dim_txt[k] = *txt; txt++; k++; }
/////////////////////////////////
	for (t = 0; t<len_a; t++){ dim_txt[k] = *txt_for_air; txt_for_air++; k++; }
	//////////////////////////////
	m = func_acs2 (alarm_A);
	for (t = 0; t<m; t++) {	dim_txt[k] = acs2_symb[t]; k++;	}
	dim_txt[k] = '\''; k++; dim_txt[k] = 'C'; k++; 
	///////////////////////////
	for (t = 0; t<len_w; t++) { dim_txt[k] = *txt_for_water; txt_for_water++; k++; }
	//////////////////////////////////
	m = func_acs2 (alarm_W);
	for(t = 0; t<m; t++){	dim_txt[k] = acs2_symb[t]; k++;	}
	dim_txt[k] = '\''; k++; dim_txt[k] = 'C'; k++; dim_txt[k] = ' '; k++; 
/////////////////////////////////
	dim_txt[k] = '\0';
/////////////////////////////////	
	send_MAIL (dim_txt, sub, false);
/*
	get_strBUF_USB(dim_txt); 
	DelaymS(150);
	get_strBUF_USB(sub);
*/	
}
////////////////////////////////////////////////////////////
void out_echo_from_ESP (void) {
//////////////////////////////////////////
	uint16_t j, i;
				for (j = 0; j < countIN_com/57; j++) {								// максимально по USB за одну посылку передаем по 57 байт, если пришло больше, то в цикле формируем несколько пакетов
						for (i = 0; i<57; i++) {
							Send_Buffer[i+7] = in_comPORT[i+j*57];
						}
					Send_Buffer[4] = 0x74; Send_Buffer[6] = 57;
					data_answer();	DelaymS (100);
				}
				if (countIN_com%57 !=0) {
							for (i = 0; i<57; i++) {
								Send_Buffer[i+7] = in_comPORT[i+j*57];
							}
						Send_Buffer[4] = 0x74; Send_Buffer[6] = countIN_com%57;
						data_answer();	DelaymS (100);
				}
//////////////////////////////////////////				
			countIN_com = 0; flag_OK_comWIFI = false; end_timeout_getdata = false;
}
////////////////////////////////////////////////////////////
void read_modework (void) {
	uint32_t data, adress;
	char* ch;
	int jk=0, j;
	int shift = 8 * 4;											// первые восемь слов заняты под таблицу с коэффициентами для уравнения расчета температуры
	adress = shift + ADDRESS_KORR_COEFF;
	
	data = flash_read (adress+jk);   //
	
	out_for_SMART.set_TMP = (uint8_t)data;									// 0-й байт установленная темп.
	jk+=4;
	
	data = flash_read (adress+jk);   //
	out_for_SMART.status_mode_GAS = (uint8_t)data;					// 0-й байт режим работы газ-эл.
	out_for_SMART.gisteresis_TMP = (uint16_t)(data>>8);			// 1-й и 2-й байты гистерезис температуры при работе с газом
	if (out_for_SMART.gisteresis_TMP>5000) out_for_SMART.gisteresis_TMP = 200;
	
	jk+=4;
	data = flash_read (adress+jk);
	out_for_SMART.work_TARIF = (uint8_t)data;								// 0-й байт режим работы по тарифу день-ночь
	out_for_SMART.gisteresis_TARIF = (uint8_t)(data>>8);		//1-й байт гистерезис для режима день-ночь
	
	jk+=4;
	time_NIGHT = flash_read (adress+jk);
	jk+=4;
	time_DAY = flash_read (adress+jk);
	if (time_NIGHT>86340) time_NIGHT = 82800;								// если время не было записано, устанавливаем 23,00
	if (time_DAY>86340) time_DAY = 25200;										// если время не было записано, устанавливаем 07,00
	
	jk+=4;
	time_save = flash_read (adress+jk);
	jk+=4;
	data = flash_read (adress+jk); 
	if (data == cool_modeON) flag_COOL = true;
	if (data == cool_modeOFF) flag_COOL = false;	
//time_NIGHT = 82800;
//time_DAY = 25200;
	
	if (out_for_SMART.status_mode_GAS != status_OK) {
		out_for_SMART.status_mode_GAS = status_NO; 
		RELE1_OFF;
//		out_for_SMART.gisteresis_TMP = 0;
	}
	if (out_for_SMART.work_TARIF != status_OK) {
		out_for_SMART.work_TARIF = status_NO;
		out_for_SMART.gisteresis_TARIF = 0;
	}
	if (out_for_SMART.set_TMP > 100) {
			out_for_SMART.set_TMP = 17;
	}
	/////////////////////////
	adress = 46*4 + ADDRESS_KORR_COEFF;
//	count = flash_read(adress+20) & 0xff;
  for (jk = 0; jk<16; jk+=4) {
		data = flash_read(adress+jk);
		init_param.SSID_wifi[jk] = 	 (unsigned char)data;
		init_param.SSID_wifi[jk+1] = (unsigned char)(data>>8);
		init_param.SSID_wifi[jk+2] = (unsigned char)(data>>16);
		init_param.SSID_wifi[jk+3] = (unsigned char)(data>>24);
	}
	for (jk = 0; jk<8; jk+=4) {
		data = flash_read (adress+jk+16);
		init_param.pass_wifi[jk] = 	 (unsigned char)data;
		init_param.pass_wifi[jk+1] = (unsigned char)(data>>8);
		init_param.pass_wifi[jk+2] = (unsigned char)(data>>16);
		init_param.pass_wifi[jk+3] = (unsigned char)(data>>24);
	}
	data = flash_read (adress+24);
	len_nameSSID = (uint8_t)data;
	///////////////////////////////////////////////////
	// забрасываем полученный IP в массив для отправки приложению
  init_param.IP_adr[0] = (unsigned char)(data>>8);
	init_param.IP_adr[1] = (unsigned char)(data>>16);
	init_param.IP_adr[2] = (unsigned char)(data>>24);
	data = flash_read (adress+28);
	init_param.IP_adr[3] = (unsigned char)(data);
	init_param.IP_adr[4] = (unsigned char)(data>>8);
	init_param.IP_adr[5] = (unsigned char)(data>>16);				
		
	init_param.IP_adr[6] = (unsigned char)(data>>24);
	data = flash_read (adress+32);
	init_param.IP_adr[7] = (unsigned char)(data);
	init_param.IP_adr[8] = (unsigned char)(data>>8);
		
	init_param.IP_adr[9] = (unsigned char)(data>>16);
	init_param.IP_adr[10] = (unsigned char)(data>>24);
	data = flash_read (adress+36);
	init_param.IP_adr[11] = (unsigned char)(data);
	///////////////////////////////////////////////////	чтение настроек почты		
	adress = 46*4 + 10*4+ ADDRESS_KORR_COEFF;
	for (jk = 0; jk<20; jk+=4) {														// считываем адрес отправителя
		data = flash_read (adress+jk);
//		mail_from[jk+11] = 	 (unsigned char)data;				
//		mail_from[jk+1+11] = (unsigned char)(data>>8);
//		mail_from[jk+2+11] = (unsigned char)(data>>16);
//		mail_from[jk+3+11] = (unsigned char)(data>>24);
    mail_from[jk+11] = 	 (unsigned char)data;				
		mail_from[jk+1+11] = (unsigned char)(data>>8);
		mail_from[jk+2+11] = (unsigned char)(data>>16);
		mail_from[jk+3+11] = (unsigned char)(data>>24);
	}
	data = flash_read (adress+20);
//		mail_from[20+11] = 	 (unsigned char)data;
//		mail_from[20+1+11] = (unsigned char)(data>>8);
//		mail_to[0+9] = (unsigned char)(data>>16);
//		mail_to[1+9] = (unsigned char)(data>>24);	
  		mail_from[20+11] = 	 (unsigned char)data;
		  mail_from[20+1+11] = (unsigned char)(data>>8);
      mail_to[0+9] = (unsigned char)(data>>16);
      mail_to[1+9] = (unsigned char)(data>>24);	
	for (jk = 0; jk<20; jk+=4) {												// считываем адрес получателя
		data = flash_read (adress+jk+24);
		//mail_to[jk+2+9] = 	 (unsigned char)data;
		//mail_to[jk+1+2+9] = (unsigned char)(data>>8);
		//mail_to[jk+2+2+9] = (unsigned char)(data>>16);
		//mail_to[jk+3+2+9] = (unsigned char)(data>>24);
    mail_to[jk+2+9] = 	 (unsigned char)data;
		mail_to[jk+3+9] = (unsigned char)(data>>8);
		mail_to[jk+4+9] = (unsigned char)(data>>16);
		mail_to[jk+5+9] = (unsigned char)(data>>24);
	}
	data = flash_read (adress+44);
  mail_name_port[0] = (unsigned char)data;          // считываем первый символ порта 
  mail_name_port[1] = (unsigned char)(data>>8);     // считываем второй символ порта  
  mail_name_port[2] = (unsigned char)(data>>16);    // считываем третий символ порта 
	mail_from_len = (unsigned char)(data>>24);		    // считываем длину адреса отправителя
  data = flash_read (adress+48);
  mail_from_len64 = (unsigned char)(data);
  mail_pass_len64 = (unsigned char)(data>>8);
  mail_to_len = (unsigned char)(data>>16);			    // считываем длину адреса получателя
  mail_name_serv_len = (unsigned char)(data>>24);   // считываем длину имени сервера отправки писем
 	for (jk = 0; jk<32; jk+=4) {											// считываем адрес отправителя в формате Base64
		data = flash_read (adress+jk+52);
		mail_from64[jk] = 	 (unsigned char)data;
		mail_from64[jk+1] = (unsigned char)(data>>8);
		mail_from64[jk+2] = (unsigned char)(data>>16);
		mail_from64[jk+3] = (unsigned char)(data>>24);
	}
	for (jk = 0; jk<28; jk+=4) {											// считываем именя сервера отправки писем
		data = flash_read (adress+jk+84);
		mail_name_serv[jk] = 	 (unsigned char)data;
		mail_name_serv[jk+1] = (unsigned char)(data>>8);
		mail_name_serv[jk+2] = (unsigned char)(data>>16);
		mail_name_serv[jk+3] = (unsigned char)(data>>24);
	}  
  data = flash_read (adress+112);
	mail_name_serv[28] = 	 (unsigned char)data;
	mail_name_serv[29] = (unsigned char)(data>>8);	
	mail_pass64[0] = (unsigned char)(data>>16);
	mail_pass64[1] = (unsigned char)(data>>24);	
	for (jk = 0; jk<24; jk+=4) {											// считываем пароль отправителя в формате Base64
		data = flash_read (adress+jk+116);
		mail_pass64[jk+2] = 	 (unsigned char)data;
		mail_pass64[jk+3] = (unsigned char)(data>>8);
		mail_pass64[jk+4] = (unsigned char)(data>>16);
		mail_pass64[jk+5] = (unsigned char)(data>>24);
	}
	data = flash_read (adress+140);
  mail_pass64[26] = (unsigned char)(data);	
  mail_pass64[27] = (unsigned char)(data>>8);	
  if ((unsigned char)(data>>24) == 0xff) account_mail = no_mailAcc;
  else account_mail = acc_mail;
 	
	ch = "MAIL FROM:<";
	for (j = 0; j<11; j++) {	mail_from[j] = *ch; ch++;	}
	mail_from [mail_from_len+11] = '>';
	mail_from_len += 12;
	
	ch = "RCPT TO:<";
	for (j = 0; j<9; j++) {	mail_to[j] = *ch; ch++;	}
	mail_to[mail_to_len+9] = '>';
	mail_to_len += 10;
}



void save_net (uint8_t data[], uint8_t flag, uint16_t len) {			// shift - смещение в записываемой области, len - длина записываемых данных(должна быть кратна 4)
	int i, j=0;
	uint16_t shift =0;
	uint32_t shift_for_koef = 46;												// первые 46 адресов массива заняты под коеф. из функции  save_modework(int shift, uint32_t data, uint32_t data1)
	uint32_t buf[256]; 																							 
	for (i = 0; i<256; i++) {															// сохраняем данные из флеша в буфере buf_COEFF[i]
		buf[i] = flash_read(ADDRESS_KORR_COEFF+j);
		j+=4;
	}
	/////////////////////////модифицируем флеш
	switch (flag) {
		case set_link:			// 10 слов задействованы под имя, его длину и пароль сети 
			shift = 0; 	
		break;
		case config_mail:		// 10 слов под сетевые настройки и еще 36 слов для майл 
			shift = 10; 	
		break;
		default:
//			shift = 10+29;		// следующие параметры будут со смещением 10 + 36
		break;
	}
  len /=4;
	for (i = 0; i<len; i++) {															// 
    buf[i+shift_for_koef+shift] = (uint32_t)data[i*4];
		buf[i+shift_for_koef+shift] = buf[i+shift_for_koef+shift] | (((uint32_t)data[i*4+1])<<8);
		buf[i+shift_for_koef+shift] = buf[i+shift_for_koef+shift] | (((uint32_t)data[i*4+2])<<16);
		buf[i+shift_for_koef+shift] = buf[i+shift_for_koef+shift] | (((uint32_t)data[i*4+3])<<24);
	}	
	///////////перезаписываем флеш
  __disable_irq();	
	FLASH_Unlock();
  FLASH_ErasePage (ADDRESS_KORR_COEFF);
  FLASH_Lock();
	DelaymS (1);
////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////// заливаем страницу из массива buf[i]
	FLASH_Unlock();
	j = 0;
	for (i = 0; i<256; i++) {
		FLASH_ProgramWord (ADDRESS_KORR_COEFF+j, buf[i]); //
		j+=4;
	}
	FLASH_Lock();
	__enable_irq();
				
	DelaymS (30);
	/////////////////////////
}



void save_modework (int shift, uint32_t data, uint32_t data1) {
	
	int i, j=0;
	USART_Cmd (USART2, DISABLE);
	for (i = 0; i<256; i++) {															// сохраняем массив коэф. считанный из флеша в буфере buf_COEFF[i]
		buf_COEFF[i] = flash_read (ADDRESS_KORR_COEFF+j);
		j+=4;
	}

	buf_COEFF[shift] = data;
	
	if (shift == save_time_modeTARIF) { 
		buf_COEFF[shift+1]= data1;   // в случае записи времени ночного и дневного тарифа модифицируем два слова (таким образом 11 и 12 слова заняты)
		buf_COEFF[save_setTMP] = out_for_SMART.set_TMP;
		buf_COEFF[time_for_save]= seconds_time;
	}
////////////////////////////////////////////////////////////////
	if (shift == save_setTMP) buf_COEFF[time_for_save]= seconds_time;
	////////////////////////////////////////////////////////////////	стираем страницу с коэффициентами
	__disable_irq();	
	FLASH_Unlock();
  FLASH_ErasePage (ADDRESS_KORR_COEFF);
  FLASH_Lock();
	DelaymS (1);
////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////// заливаем страницу с коэффициентами из массива buf_COEFF[i]
	FLASH_Unlock();
	j=0;
	
	for (i = 0; i<256; i++) {
		FLASH_ProgramWord (ADDRESS_KORR_COEFF+j, buf_COEFF[i]); //
		j+=4;
	}
	
	FLASH_Lock();
	__enable_irq();
	USART_Cmd (USART2, ENABLE);
	DelaymS (30);
	
}



void func_get_data_TCP (uint16_t start_b) {
			uint8_t i = 6 + 64;
			uint8_t kk;
			int crc_send, crc_check;
      uint8_t dim_crc_mail[144];
	
			TIM_Cmd (TIM2, DISABLE);
      PARAM.count_COMAND_ = dim_for_SMART[3+64];
      PARAM.COMAND_ = dim_for_SMART[4+64];
  
      if (PARAM.COMAND_ != config_mail) {
        for (kk = 128; kk<192; kk++) { 
          if (dim_for_SMART[kk]==127) dim_for_SMART[kk-64] = dim_for_SMART[kk-64]+128;
        }
        crc_check = CalculateCRC (dim_for_SMART, 123); // вычисляем контрольную сумму по 123 байт массива включительно
        crc_send = (uint32_t)dim_for_SMART[124] | (((uint32_t)dim_for_SMART[125])<<8) | (((uint32_t)dim_for_SMART[126])<<16) | (((uint32_t)dim_for_SMART[127])<<24);
      } else { 
        for (kk = 0; kk<52; kk++) {
          dim_crc_mail[kk] = dim_for_SMART[4+kk];
        }
        for (kk = 52; kk<142; kk++) {
          dim_crc_mail[kk] = dim_for_SMART[17+kk];
        }  
        crc_check = (uint16_t)CalculateCRC (dim_crc_mail, 142);      
        crc_send = (uint16_t)dim_for_SMART[159] | (((uint16_t)dim_for_SMART[160])<<8);
        crc_check = (0x007f & crc_check) | ((0x007f & (crc_check>>8))<<8);
      }
			
			
			if (PARAM.COMAND_ != cmd_save_kf && PARAM.COMAND_ != set_link && PARAM.COMAND_ != config_mail) {
				PARAM.time_NIGHT_ = (int)dim_for_SMART[i]; i++;
				PARAM.time_NIGHT_ = (((int)dim_for_SMART[i])<<8) | PARAM.time_NIGHT_; i++;
				PARAM.time_NIGHT_ = (((int)dim_for_SMART[i])<<16) | PARAM.time_NIGHT_; i++;
				PARAM.time_DAY_ = (int)dim_for_SMART[i]; i++;
				PARAM.time_DAY_ = (((int)dim_for_SMART[i])<<8) | PARAM.time_DAY_; i++;
				PARAM.time_DAY_ = (((int)dim_for_SMART[i])<<16) | PARAM.time_DAY_; i++;
				PARAM.work_GAS = dim_for_SMART[i]; i++;
				PARAM.work_TARIF_ = dim_for_SMART[i]; i++;
				PARAM.set_TMP_ = dim_for_SMART[i]; i++;   /// Free bait, not realised 
				//////////////////////////////////////////////
				PARAM.SECOND_ = (int)dim_for_SMART[i]; i++;
				PARAM.SECOND_ = (((int)dim_for_SMART[i])<<8) | PARAM.SECOND_; i++;
				PARAM.SECOND_ = (((int)dim_for_SMART[i])<<16) | PARAM.SECOND_; i++;
				//////////////////////////////////////////////
				PARAM.gisteresis_TMP_ = (uint16_t)dim_for_SMART[i]; i++;
				PARAM.gisteresis_TMP_ = (((uint16_t)dim_for_SMART[i])<<8) | PARAM.gisteresis_TMP_; i++;
				PARAM.gisteresis_TARIF_ = dim_for_SMART[i]; i++;
				PARAM.delta_tmp_ = dim_for_SMART[i]; i++;
				PARAM.sys_DATA[0] = dim_for_SMART[i]; i++;
				PARAM.sys_DATA[1] = dim_for_SMART[i]; i++;
				PARAM.sys_DATA[2] = dim_for_SMART[i]; i++;
				PARAM.sys_DATA[3] = dim_for_SMART[i]; i++;
        PARAM.TMPBR_ = (uint16_t)dim_for_SMART[i]; i++;
        PARAM.TMPBR_ = (((uint16_t)dim_for_SMART[i])<<8) | PARAM.TMPBR_; i++;
        PARAM.gistTMPBR_ = dim_for_SMART[i]; i++;
        PARAM.flagBR_ = dim_for_SMART[i]; i++;
        PARAM.flag_AlarmOFF = dim_for_SMART[i]; i++;
			}
			if (crc_check == crc_send) {											// проверка контрольной суммы, полученное значение и подсчитаное
				if (PARAM.count_COMAND_ != count_last_cmd) {		// проверка полученной команды на дубль 			
					buf_data[2] = 77;
					flag_answer_TCP = true;
					change_tmp = PARAM.delta_tmp_;
					seconds_time = PARAM.SECOND_;
					count_last_cmd = PARAM.count_COMAND_;
					if (PARAM.COMAND_ == set_link) flag_start_sendWIFI = false;
				} else buf_data[2] = 88;		
				out_for_SMART.count_COMAND = PARAM.count_COMAND_;
				out_for_SMART.COMAND = PARAM.COMAND_;
			}
			DelaymS(80);
			send_str_USB ("Command: ", PARAM.COMAND_);
			DelaymS (80);
			send_str_USB ("Count command: ", PARAM.count_COMAND_);
			// по новому алгоритму нужно изменить заполнение начальных байт буфера отправки данных клиенту! 04.07.2018			
//			for(i = 0; i<50; i++){ data_OUT_COM[i] = dim_for_SMART[i];}	
			buf_data[56] = (uint8_t)(crc_check & 0xff); buf_data[57] = (uint8_t)((crc_check>>8) & 0xff); buf_data[58] = (uint8_t)((crc_check>>16) & 0xff); buf_data[59] = (uint8_t)((crc_check>>24) & 0xff);
			buf_data[60] = dim_for_SMART[124]; buf_data[61] = dim_for_SMART[125]; buf_data[62] = dim_for_SMART[126]; buf_data[63] = dim_for_SMART[127];
			TIM_Cmd (TIM2, ENABLE);
}



uint32_t CalculateCRC (uint8_t dim_crc[], uint32_t size) {
    uint32_t  CRC32 = 0;
//		uint8_t tmp;
    while ( size != 0 ) {
        CRC32 = CRC32 + ((uint32_t)dim_crc[size])*size;
        size--;
    }
		CRC32 = CRC32&0xFFFFFFFF;
    return CRC32;
}



void func_heat (uint8_t GAS, bool flg_cool, bool heat) {
	if (heat) {																												// включаем нагрев
			if (!flg_cool) {																							// инверсный выход
				if (GAS == status_OK) { RELE1_ON; } else { RELE2_ON; }
			} else {																											// прямой выход
				if (GAS == status_OK) { RELE1_OFF; } else { RELE2_OFF; }
			}
			out_for_SMART.status_HEAT = status_OK;
  } else {																												// выключаем нагрев	
  		if (!flg_cool) {
				if (GAS == status_OK) { RELE1_OFF; } else { RELE2_OFF; }
			} else {
				if (GAS == status_OK) { RELE1_ON; } else { RELE2_ON; }
			}				
			out_for_SMART.status_HEAT = status_NO;
  }
}



void func_synchro (void) {
					if (out_for_SMART.work_TARIF == status_OK && !flag_sync_setTMP_tarif) {													// если работаем по дневному-ночному тарифу		
						if (time_NIGHT > time_DAY) { 
							if (seconds_time < time_NIGHT && seconds_time > time_DAY) flag_now_DAY = true;
							else flag_now_DAY = false;
						}	else { 
							if (seconds_time > time_NIGHT && seconds_time < time_DAY)	flag_now_DAY = false;
							else flag_now_DAY = true;							 
						}
						if (flag_now_DAY) {																								// на данный момент действует дневной тариф			
								if (time_save < time_NIGHT && time_save > time_DAY) { 	 				// последняя установка температуры проводилась во время действия дневного тарифа
									flag_debag = 1;
								}	else {																													// последняя установка температуры проводилась во время действия ночного тарифа
									out_for_SMART.set_TMP = out_for_SMART.set_TMP	- out_for_SMART.gisteresis_TARIF;			
									flag_debag = 2;	
								}
						} else {																													// на данный момент действует ночной тариф													
								if (time_save < time_NIGHT && time_save > time_DAY) { 				// последняя установка температуры проводилась во время действия дневного тарифа
									out_for_SMART.set_TMP = out_for_SMART.set_TMP	+ out_for_SMART.gisteresis_TARIF;
									flag_debag = 3;
								}	else flag_debag = 4;																				// последняя установка температуры проводилась во время действия ночного тарифа

						}		
						flag_sync_setTMP_tarif = true;										// выставлен флаг, чтобы при дальнейшей синхронизации значение установленной температуры не менялось
								led_one_ON(LED_R);
								DelaymS(1000);
								led_one_OFF(LED_R);							
					}				
}



#ifdef  USE_FULL_ASSERT

void assert_failed(uint8_t* file, uint32_t line)
{ 
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  /* Infinite loop */
  while (1)
  {
  }
}
#endif


