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
                color = R.color.azulito;
                break;

            case R.id.verdecito:
                color = R.color.verdecito;
                break;

            case R.id.rojito:
                color = R.color.rojito;
                break;

            case R.id.amarillito:
                color = R.color.amarillito;
                break;

            case R.id.naranjita:
                color = R.color.naranjita;
                break;

            case R.id.gold:
                color = R.color.gold;
                break;

            case R.id.silver:
                color = R.color.silver;
                break;

            case R.id.bronze:
                color = R.color.bronze;
                break;

            case R.id.grey:
                color = R.color.grey;
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