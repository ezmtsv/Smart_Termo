#include		"func.h"
#include <math.h>
#include <string.h>
#include <stdbool.h>


uint16_t buffer_ADC[200];
uint16_t int_charge;
volatile buffer_OUT_SMART out_for_SMART;
int dataINint[34];
uint8_t buf_data[64];
double alarm_A;
double alarm_W;
double temp_boiler;
double temp_outdoor;
bool flag_alarm_A = false;
bool flag_alarm_W = false;
bool flag_alarm_HeaterOFF = false;
double cur_charge;
const double U_pow = 2.81;				//;



extern uint16_t ADC_stm;
extern uint8_t Send_Buffer[64];
extern unsigned int time_DAY;
extern unsigned int time_NIGHT;
extern bool flag_COOL;
extern bool charge_ON;
extern bool no_accum;


void Delay(int nTime);
void DelaymS(__IO uint32_t nCount);
void DelayuS(__IO uint32_t nCount);
void data_answer(void);



//////////////////////////////////////фильтр медиана (кол-во элементов массива должно быть четное)//////////////////////////////////////
uint16_t filter_median (uint16_t *buf, uint16_t len) {
	uint16_t count;
	uint16_t tmp_count;
	uint16_t maximum;
	uint16_t rezult;
///////////////////располагаем массив данных  по возрастанию 
	rezult = 0;
	for (tmp_count = (len-1); tmp_count>0; tmp_count--) {
		for (count = 0; count<tmp_count; count++) {
			if (buf[count]>buf[count+1]) {
				maximum = buf[count];
				buf[count] = buf[count+1];
				buf[count+1] = maximum;
			};
		}
	}
  rezult = buf[len/2];
	return rezult;
}



uint16_t filter_RMS (uint16_t *buf, uint16_t len) {
		int64_t res_tmp = 0;
		int64_t res_tmp1 = 0;
		uint16_t res_ = 0;
		double kf;
		int ii;
		for (ii = 0; ii<len; ii++) {
			res_tmp1 = (int64_t)buf[ii];
			res_tmp = res_tmp + res_tmp1*res_tmp1;
		}
		kf = (double)res_tmp/(double)len; 
		kf = sqrt(kf);
		res_ = (uint16_t)kf;
		return res_;
	}



void work_ADC_STM (void) {
	uint16_t count; 
  ADC1->CHSELR = ADC_CHSELR_CHSEL6;	                    // вход АЦП подключенный для изм. темп. воздуха
	for (count = 0; count<100; count++) {
    buffer_ADC[count] = run_ADCSTM();
    Delay(300);
  }
  out_for_SMART.tmp_A = filter_median (buffer_ADC, 100);	
//  out_for_SMART.tmp_A = data_ADC_filter;
  
	ADC1->CHSELR = ADC_CHSELR_CHSEL5;                     // вход АЦП подключенный для изм. темп. воды 
	for (count = 0; count<100; count++) {
    buffer_ADC[count] = run_ADCSTM();
    Delay(300);
  }
  out_for_SMART.tmp_W = filter_median (buffer_ADC, 100);
//  Send_Buffer[22] = (uint8_t)(data_ADC_filter);
//  Send_Buffer[23] = (uint8_t)(data_ADC_filter>>8);
//  out_for_SMART.tmp_W = data_ADC_filter;	
  
	ADC1->CHSELR = ADC_CHSELR_CHSEL7;											// вход АЦП подключенный к аккум.
	pinCHARGE_OFF;																	      //выключаем заряд аккум.						
  for (count = 0; count<100; count++) {
    buffer_ADC[count] = run_ADCSTM();
    Delay(300);
  }
  int_charge = filter_median (buffer_ADC, 100);
//  int_charge = data_ADC_filter;				
  if (charge_ON || no_accum) pinCHARGE_ON;							// если зарядка была активна, включаем ее снова, в отсутствии аккум. тоже включаем, а в основном цикле выключаем(без аккум - будет мигать)
///////////////////////////////////
/*				
  for (count = 0; count<100; count++) {
    buffer_ADC[count] = run_ADCSTM();
    Delay(300);
  }
  data_ADC_filter = filter_median (buffer_ADC, 100);
  int_charge = data_ADC_filter;		
	if (no_accum) pinCHARGE_ON;							              //  в отсутствии аккум. включаем, а в основном цикле выключаем(без аккум - будет мигать)
*/
	ADC1->CHSELR = ADC_CHSELR_CHSEL8;                     // вход АЦП подключенный для изм. темп. воды бойлера
	for (count = 0; count<100; count++) {
    buffer_ADC[count] = run_ADCSTM();
    Delay(300);
  }
  out_for_SMART.tmp_B = filter_median (buffer_ADC, 100);

	ADC1->CHSELR = ADC_CHSELR_CHSEL9;                     // вход АЦП подключенный для изм. темп. воздуха на улице 
	for (count = 0; count<100; count++) {
    buffer_ADC[count] = run_ADCSTM();
    Delay(300);
  }
  out_for_SMART.tmp_O = filter_median (buffer_ADC, 100);
  
  memset (buffer_ADC,0, sizeof (buffer_ADC));				
}	



uint16_t run_ADCSTM (void) {
	uint16_t result_ADC;
  
  ADC_StartOfConversion (ADC1);
  while (ADC_GetFlagStatus (ADC1, ADC_FLAG_EOC) == RESET);
  result_ADC = ADC_GetConversionValue (ADC1);
	return result_ADC;
}



extern unsigned int time_save;
extern uint8_t flag_debag;
extern int seconds_start_server;
extern uint16_t set_gisttempBoiler;
extern uint8_t flag_work_boiler;
extern uint16_t set_tempBoiler;

void init_out_for_SMART (buffer_OUT_SMART* out) {
	int i = 6;	
//	for (j = 0; j<64; j++) buf_data[j] = j+129;
	buf_data[3] = out->count_COMAND; 
	buf_data[4] = out->COMAND; 	
	
	buf_data[i] = (uint8_t)out->count_off_POW; i++;
	buf_data[i] = (uint8_t)(out->count_off_POW>>8); i++;
	buf_data[i] = (uint8_t)out->count_work_HEAT; i++;
	buf_data[i] = (uint8_t)(out->count_work_HEAT>>8); i++;
	buf_data[i] = out->status_BUSY; i++;
	buf_data[i] = out->status_HEAT; i++;
	buf_data[i] = out->status_power220; i++;
	buf_data[i] = (uint8_t)out->tmp_AIR; i++;
	buf_data[i] = (uint8_t)(out->tmp_AIR>>8); i++;
	buf_data[i] = (uint8_t)out->tmp_WATER; i++;
	buf_data[i] = (uint8_t)(out->tmp_WATER>>8); i++;
	buf_data[i] = out->set_TMP; i++;
	buf_data[i] = out->status_mode_GAS; i++;
	buf_data[i] = out->work_TARIF; i++;
	buf_data[i] = (uint8_t)out->gisteresis_TMP; i++;
	buf_data[i] = (uint8_t)(out->gisteresis_TMP>>8); i++;
	buf_data[i] = out->gisteresis_TARIF; i++;
	buf_data[i] = out->znaki_TMP; i++;
	buf_data[i] = (uint8_t)out->tmp_A; i++;
	buf_data[i] = (uint8_t)(out->tmp_A>>8); i++;
	buf_data[i] = (uint8_t)out->tmp_W; i++;
	buf_data[i] = (uint8_t)(out->tmp_W>>8); i++;
	buf_data[i] = (uint8_t)(out->SECOND); i++;		    // 28
	buf_data[i] = (uint8_t)(out->SECOND>>8); i++;		  // 29
	buf_data[i] = (uint8_t)(out->SECOND>>16); i++;	  // 30
	buf_data[i] = (uint8_t)(time_NIGHT); i++;
	buf_data[i] = (uint8_t)(time_NIGHT>>8); i++;
	buf_data[i] = (uint8_t)(time_NIGHT>>16); i++;
	buf_data[i] = (uint8_t)(time_DAY); i++;
	buf_data[i] = (uint8_t)(time_DAY>>8); i++;
	buf_data[i] = (uint8_t)(time_DAY>>16); i++;		    //36 байт

	buf_data[i] = (uint8_t)(time_save); i++;
	buf_data[i] = (uint8_t)(time_save>>8); i++;
	buf_data[i] = (uint8_t)(time_save>>16); i++;
	
	if (flag_COOL)buf_data[i] = status_OK;		        // 40 байт
	else { buf_data[i] = status_NO; } ; i++;
	
	buf_data[i] = (uint8_t)(seconds_start_server); i++;
	buf_data[i] = (uint8_t)(seconds_start_server>>8); i++;
	buf_data[i] = (uint8_t)(seconds_start_server>>16); i++;
	buf_data[i] = (uint8_t)(cur_charge*10); i++;
	buf_data[i] = (uint8_t)(((uint16_t)(cur_charge*100))%100); i++;
  buf_data[i] = (uint8_t)out->tmp_Boiler; i++;
  buf_data[i] = (uint8_t)(out->tmp_Boiler>>8); i++;
  buf_data[i] = (uint8_t)out->tmp_Outdoor; i++;
  buf_data[i] = (uint8_t)(out->tmp_Outdoor>>8); i++;
  buf_data[i] = (uint8_t)(set_gisttempBoiler/100); i++;
  buf_data[i] = (uint8_t)flag_work_boiler; i++;
  buf_data[i] = (uint8_t)(set_tempBoiler/100); i++;
  if (flag_alarm_HeaterOFF) buf_data[i] = 1;
  else buf_data[i] = 0; i++;
  
	buf_data[i] = (uint8_t)out->tmp_O; i++;
  buf_data[i] = (uint8_t)(out->tmp_O>>8); i++;
	buf_data[i] = (uint8_t)out->tmp_B; i++;
  buf_data[i] = (uint8_t)(out->tmp_B>>8); i++;
	
//	buf_data[i] = flag_debag; i++;
//	buf_data[i] = 43; i++;
/*	for(j = 0; j<17; j++){
		buf_data[i] = (uint8_t)dataINint[j]; i++;
	}*/
}

void real_temp (void) {
//        String [] rltemp = new String[2];
/*
        int A1, A2, A3, A4, tempA, tempW, A1E, A2E, A3E, A4E;
        double A1doub, A2doub, A3doub, A4doub, tempAdoub, tempWdoub;
        //получаем из массива dataIN калибровочные коэффициенты и данные АЦП
        A1 = dataINint[1]| (dataINint[2]<<8)|(dataINint[3]<<16);
        A2 = dataINint[5]| (dataINint[6]<<8)|(dataINint[7]<<16);
        A3 = dataINint[9]| (dataINint[10]<<8)|(dataINint[11]<<16);
        A4 = dataINint[13]| (dataINint[14]<<8)|(dataINint[15]<<16);
        ////////////////////////////////////получение знаков коэф.///////
        if((dataINint[0] & 0x01)!=0) { A1 = A1*-1;}
        if((dataINint[0] & 0x02)!=0) { A2 = A2*-1;}
        if((dataINint[0] & 0x04)!=0) { A3 = A3*-1;}
        if((dataINint[0] & 0x08)!=0) { A4 = A4*-1;}
////////////////////////////////////получение знаков и порядка степени ///////
        A1E = dataINint[4]; if((A1E & 0x80)!=0){ A1E = (0x7f & A1E)*-1;}
        A2E = dataINint[8]; if((A2E & 0x80)!=0){ A2E = (0x7f & A2E)*-1;}
        A3E = dataINint[12]; if((A3E & 0x80)!=0){ A3E = (0x7f & A3E)*-1;}
        A4E = dataINint[16]; if((A4E & 0x80)!=0){ A4E = (0x7f & A4E)*-1;}

        tempA = out_for_SMART.tmp_A;                                               //температуру воздуха
        tempW = out_for_SMART.tmp_W;                                                 //температуру воды


        A1doub = ((double)A1)/10000.0; A2doub = ((double)A2)/10000.0; A3doub = ((double)A3)/10000.0; A4doub = ((double)A4)/10000.0;
        tempAdoub = A1doub*pow(10, A1E)*pow(tempA, 3)+A2doub*pow(10, A2E)*pow(tempA, 2)+A3doub*pow(10, A3E)*tempA+A4doub*pow(10, A4E);
//       rltemp[0] = two_symbol_after_point(Double.toString(tempAdoub));
//				tempAdoub = -11.58;
/////////////////////////////////////////////////
				alarm_A = tempAdoub; 			// сохраняем текущую темп. возд. в переменной alarm_A для обработки событий ALARM
				if(tempAdoub<alarm_tempAIR){	flag_alarm_A = true; }
////////////////////////////////////////////////				
				if(tempAdoub < 0){ out_for_SMART.znaki_TMP = 1; tempAdoub = tempAdoub * -1; } else {out_for_SMART.znaki_TMP = 0;}
					
        tempWdoub = A1doub*pow(10, A1E)*pow(tempW, 3)+A2doub*pow(10, A2E)*pow(tempW, 2)+A3doub*pow(10, A3E)*tempW+A4doub*pow(10, A4E);
//       rltemp[1] = two_symbol_after_point(Double.toString(tempAdoub));
//				tempWdoub = -145.67;
/////////////////////////////////////////////////////////
				alarm_W = tempWdoub; 			// сохраняем текущую темп. теплонос. в переменной alarm_W для обработки событий ALARM
				if(tempWdoub>alarm_tempWATER){ 	flag_alarm_W = true; }	
/////////////////////////////////////////////////////////				
				if(tempWdoub < 0){ out_for_SMART.znaki_TMP = out_for_SMART.znaki_TMP | 0x02; tempWdoub = tempWdoub*-1; } ;
				out_for_SMART.tmp_AIR = (uint16_t)(tempAdoub*100);			
				out_for_SMART.tmp_WATER = (uint16_t)(tempWdoub*100);
///////////////////////////////////////////////////debug
//				out_for_SMART.tmp_AIR = 3456;			
//				out_for_SMART.tmp_WATER = 9045;
///////////////////////////////////////////////////debug	
*/		
  int A0, A1, A2, A3, A4, tempA, tempW, A0E, A1E, A2E, A3E, A4E;
  double A0doub, A1doub, A2doub, A3doub, A4doub, tempAdoub, tempWdoub;
  //получаем из массива dataIN калибровочные коэффициенты и данные АЦП
	A0 = dataINint[17]| (dataINint[18]<<8)|(dataINint[19]<<16);
  A1 = dataINint[1]| (dataINint[2]<<8)|(dataINint[3]<<16);
  A2 = dataINint[5]| (dataINint[6]<<8)|(dataINint[7]<<16);
  A3 = dataINint[9]| (dataINint[10]<<8)|(dataINint[11]<<16);
  A4 = dataINint[13]| (dataINint[14]<<8)|(dataINint[15]<<16);
  ////////////////////////////////////получение знаков коэф.///////
  if ((dataINint[0] & 0x01)!=0) A1 = A1*-1;
  if ((dataINint[0] & 0x02)!=0) A2 = A2*-1;
  if ((dataINint[0] & 0x04)!=0) A3 = A3*-1;
  if ((dataINint[0] & 0x08)!=0) A4 = A4*-1;
	if ((dataINint[19] & 0x80)!=0) A0 = (A0 & 0x7fffff)*-1; 
  ////////////////////////////////////получение знаков и порядка степени ///////
	A0E = (dataINint[0]>>4)*-1;
  A1E = dataINint[4]; if((A1E & 0x80)!=0){ A1E = (0x7f & A1E)*-1;}
  A2E = dataINint[8]; if((A2E & 0x80)!=0){ A2E = (0x7f & A2E)*-1;}
  A3E = dataINint[12]; if((A3E & 0x80)!=0){ A3E = (0x7f & A3E)*-1;}
  A4E = dataINint[16]; if((A4E & 0x80)!=0){ A4E = (0x7f & A4E)*-1;}

  tempA = out_for_SMART.tmp_A;                                               //температуру воздуха
  tempW = out_for_SMART.tmp_W;                                               //температуру воды

  A0doub = ((double)A0)/10000.0; A1doub = ((double)A1)/10000.0; A2doub = ((double)A2)/10000.0; A3doub = ((double)A3)/10000.0; A4doub = ((double)A4)/10000.0;
  tempAdoub = A0doub*pow(10, A0E)*pow(tempA, 4)+A1doub*pow(10, A1E)*pow(tempA, 3)+A2doub*pow(10, A2E)*pow(tempA, 2)+A3doub*pow(10, A3E)*tempA+A4doub*pow(10, A4E);
  //  rltemp[0] = two_symbol_after_point(Double.toString(tempAdoub));
  //	tempAdoub = -11.58;
  /////////////////////////////////////////////////
	alarm_A = tempAdoub; 			// сохраняем текущую темп. возд. в переменной alarm_A для обработки событий ALARM
	if (tempAdoub<alarm_tempAIR) flag_alarm_A = true;
  ////////////////////////////////////////////////				
	if (tempAdoub < 0) { 
    out_for_SMART.znaki_TMP = 1; 
    tempAdoub = tempAdoub * -1; 
  } else out_for_SMART.znaki_TMP = 0;
					
  tempWdoub = A0doub*pow(10, A0E)*pow(tempW, 4)+A1doub*pow(10, A1E)*pow(tempW, 3)+A2doub*pow(10, A2E)*pow(tempW, 2)+A3doub*pow(10, A3E)*tempW+A4doub*pow(10, A4E);
  //       rltemp[1] = two_symbol_after_point(Double.toString(tempAdoub));
  //				tempWdoub = -145.67;
  /////////////////////////////////////////////////////////
	alarm_W = tempWdoub; 			// сохраняем текущую темп. теплонос. в переменной alarm_W для обработки событий ALARM
	if (tempWdoub>alarm_tempWATER)flag_alarm_W = true;	
	else flag_alarm_W = false;
  /////////////////////////////////////////////////////////				
	if (tempWdoub < 0) { 
    out_for_SMART.znaki_TMP = out_for_SMART.znaki_TMP | 0x02; 
    tempWdoub = tempWdoub*-1; 
  } else out_for_SMART.znaki_TMP &= ~0x02;
	out_for_SMART.tmp_AIR = (uint16_t)(tempAdoub*100);			
	out_for_SMART.tmp_WATER = (uint16_t)(tempWdoub*100);

  tempA = out_for_SMART.tmp_O;                                               //температура воздуха на улице
  tempW = out_for_SMART.tmp_B;                                               //температура воды в бойлере  
  tempAdoub = A0doub*pow(10, A0E)*pow(tempA, 4)+A1doub*pow(10, A1E)*pow(tempA, 3)+A2doub*pow(10, A2E)*pow(tempA, 2)+A3doub*pow(10, A3E)*tempA+A4doub*pow(10, A4E);
  tempWdoub = A0doub*pow(10, A0E)*pow(tempW, 4)+A1doub*pow(10, A1E)*pow(tempW, 3)+A2doub*pow(10, A2E)*pow(tempW, 2)+A3doub*pow(10, A3E)*tempW+A4doub*pow(10, A4E);
  temp_outdoor = tempAdoub;
  temp_boiler =  tempWdoub;
  if (tempAdoub < 0) { 
    out_for_SMART.znaki_TMP |= 4; 
    tempAdoub = tempAdoub * -1; 
  } else out_for_SMART.znaki_TMP &= ~0x04;
	if (tempWdoub < 0) { 
    out_for_SMART.znaki_TMP |= 8; 
    tempWdoub = tempWdoub*-1; 
  } else out_for_SMART.znaki_TMP &= ~0x08;
	out_for_SMART.tmp_Outdoor = (uint16_t)(tempAdoub*100);			
	out_for_SMART.tmp_Boiler = (uint16_t)(tempWdoub*100);    
  ///////////////////////////////////////////////////debug
  //				out_for_SMART.tmp_AIR = 3456;			
  //				out_for_SMART.tmp_WATER = 9045;
  ///////////////////////////////////////////////////debug	
	cur_charge = ((U_pow*(double)(int_charge))/4096)*1.5;		// расчитываем текущее значение напряжения на аккум.
}

