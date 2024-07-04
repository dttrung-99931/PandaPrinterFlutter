package com.print_plugin.print.utils;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.util.Log;

import kotlin.jvm.JvmStatic;

public class TextUtils {
   public static Boolean isFullWidth(String character) {
        int esw;
        try {
            esw = UCharacter.getIntPropertyValue(character.charAt(0), UProperty.EAST_ASIAN_WIDTH);
        } catch (Exception e) {
//            Log.d(
//                    "",
//                    character + " false 2 (" + e + ")"
//            );
            return false;
        }

//        Log.d(
//                "",
//                character + (
//                        (esw == UCharacter.EastAsianWidth.FULLWIDTH || esw == UCharacter.EastAsianWidth.WIDE) ?
//                                " is full" : " is half")
//        );
        return esw == UCharacter.EastAsianWidth.FULLWIDTH || esw == UCharacter.EastAsianWidth.WIDE;
    }
}
