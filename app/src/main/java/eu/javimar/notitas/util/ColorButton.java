package eu.javimar.notitas.util;

import eu.javimar.notitas.R;

public class ColorButton
{
    public static int colorButton(int id)
    {
        int color;

        switch(id)
        {
            case R.id.azulito:
                color = R.color.c1;
                break;

            case R.id.verdecito:
                color = R.color.c2;
                break;

            case R.id.rojito:
                color = R.color.c3;
                break;

            case R.id.amarillito:
                color = R.color.c4;
                break;

            case R.id.naranjita:
                color = R.color.c5;
                break;

            case R.id.gold:
                color = R.color.c6;
                break;

            case R.id.silver:
                color = R.color.c7;
                break;

            case R.id.bronze:
                color = R.color.c8;
                break;

            case R.id.grey:
                color = R.color.c9;
                break;

            case R.id.purple:
                color = R.color.purple;
                break;

            case R.id.white:

            default:
                color = R.color.white;
        }
        return color;
    }
}