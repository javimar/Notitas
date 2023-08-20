package eu.javimar.notitas.util;

import eu.javimar.notitas.R;

public class ColorButton {
    public static int colorButton(int id) {
        int color;

        switch (id) {
            case R.id.c1:
                color = R.color.c1;
                break;

            case R.id.c2:
                color = R.color.c2;
                break;

            case R.id.c3:
                color = R.color.c3;
                break;

            case R.id.c4:
                color = R.color.c4;
                break;

            case R.id.c5:
                color = R.color.c5;
                break;

            case R.id.c6:
                color = R.color.c6;
                break;

            case R.id.c7:
                color = R.color.c7;
                break;

            case R.id.c8:
                color = R.color.c8;
                break;

            case R.id.c9:
                color = R.color.c9;
                break;

            case R.id.c10:
                color = R.color.c10;
                break;

            case R.id.white:

            default:
                color = R.color.white;
        }
        return color;
    }
}