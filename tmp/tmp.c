  if (flag_work_boiler) {
    if (set_tempBoiler > out_for_SMART.tmp_Boiler) {                                                            // переключение на бойлер (при старте модуля бойлер по умолчанию всегда выключен - flag_work_boiler = 0)  
      RELE3_ON;
      GREEN_DISP_OFF;
      RED_DISP_ON;
      flag_st_heat_boiler = true;                                                                               // включился нагрев воды 
      if (!flag_alarm_W && !flag_alarm_HeaterOFF) func_heat (out_for_SMART.status_mode_GAS, flag_COOL, true);   // включить нагрев
      else {
        if (!flag_alarm_HeaterOFF) get_strBUF_USB ("ALARM Boiler! Overheating water!");                         // температура воды выше критической  или принудительное аварийное откл., выключить нагрев
        else get_strBUF_USB ("ALARM Heater OFF enabled!");
        func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);								            // выключить нагрев
      }    
    } else {
//      if ((set_tempBoiler + set_gisttempBoiler) < out_for_SMART.tmp_Boiler) { 
      if ((out_for_SMART.tmp_Boiler-set_tempBoiler)>set_gisttempBoiler) {               
        if (flag_st_heat_system) RELE3_OFF;												                        // если идет нагрев системы отопления переключаем насос
		else func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);						                // иначе выключить нагрев и оставить циркуляцию воды через бойлер
        RED_DISP_OFF;
        GREEN_DISP_ON;
        
        flag_st_heat_boiler = false;                                                                            // нагрев воды выключился
      }    
    }
  } else {
    RED_DISP_OFF;
    GREEN_DISP_OFF;  
    flag_st_heat_boiler = false;
    RELE3_OFF;
  }
  if (!flag_st_heat_boiler) {                                                                                   // если в данный момент не идет нагрев воды 
    delta_TMP = ((int16_t)out_for_SMART.set_TMP)*100 - out_for_SMART.tmp_AIR;		                            // находим разницу между реальной температурой возд. и установленной
    if (!flag_alarm_W && !flag_alarm_HeaterOFF) {																// если темп. воды не превышает критическую 95гр.цельс.
      if ((uint16_t)out_for_SMART.set_TMP*100 > out_for_SMART.tmp_AIR) {
        func_heat (out_for_SMART.status_mode_GAS, flag_COOL, true);								                // включить нагрев
		flag_st_heat_system = true;
	  }
      else {
        if (((uint16_t)out_for_SMART.set_TMP*100 + out_for_SMART.gisteresis_TMP) < out_for_SMART.tmp_AIR) {
          func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false);							                // выключить нагрев
          flag_st_heat_system = false;
		}
      }
    } else { 																									// температура воды выше критической или принудительное аварийное откл., выключить нагрев
      if (!flag_alarm_HeaterOFF) get_strBUF_USB ("ALARM! Overheating water!_"); 
      else get_strBUF_USB ("ALARM Heater OFF enabled!_");
      func_heat (out_for_SMART.status_mode_GAS, flag_COOL, false); 
    }  
  }