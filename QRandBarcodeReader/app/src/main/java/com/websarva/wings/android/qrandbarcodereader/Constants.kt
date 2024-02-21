package com.websarva.wings.android.qrandbarcodereader

/*  定数クラス     */
object Constants {

    /*  文字定数 */
    const val TXT_DEF_BT_ADRS = "D8:80:39:F6:01:AF"     //文字定数 Bluetoothアドレス プリンタ
    //const val TXT_DEF_BT_ADRS = "00:A5:54:C2:D7:96"     //文字定数 Bluetoothアドレス デスクトップPC
    const val TXT_DEF_BLANK = ""                        //文字定数 BLANK

    /*  key名    */
    const val KEY_BT_ADRS = "KEY_BT_ADRS"               //Bluetoothアドレス入力欄で使う
    const val KEY_SEND_VAL = "KEY_SEND_VAL"             //送信値入力欄で使う
    const val KEY_STAT_TRANS_BT_ADRS = "KEY_STAT_TRANS_BT_ADRS" //画面遷移時、Bluetoothアドレス送信キー

    /*  プリファレンスファイル名  */
    const val PREF_INPUT_VALUES = "inputValues"              //入力値を保存するプリファレンス
}