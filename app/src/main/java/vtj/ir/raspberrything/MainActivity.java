package vtj.ir.raspberrything;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;

import java.io.IOException;
import java.util.List;

/**
 * Created by javadroid.ir on 9/22/17.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RPI_ANDROID";
    private static final String BOT_TOKEN = "YOUR_SWEET_BOT_TOKEN :)";

    private final String PIN_LED_BLUE = "BCM17";
    private final String PIN_LED_WHITE = "BCM27";
    private final String PIN_BEEZER = "BCM22";
    boolean[] situation = {false, false, false};

    private Gpio mGpioBlue;
    private Gpio mGpioWhite;
    private Gpio mGpioBeezer;
    PeripheralManagerService service;

    TelegramBot bot;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        service = new PeripheralManagerService();
        tv = (TextView) findViewById(R.id.tv);
        Log.e(TAG, "GPIOs: " + service.getGpioList());
        initPins();

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }
        });

        bot = TelegramBotAdapter.build(BOT_TOKEN);
        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(10);
        bot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> updates) {
                for (Update update : updates) {
                    Message msg = update.message();
                    if (msg != null) {
                        String txt = msg.text();
                        tv.append("\n" + txt);
                        if (txt.trim().startsWith("LED")) {
                            Log.d(TAG, "LED COMMAND");
                            String[] data = txt.split(" ");
                            if (data[1].equalsIgnoreCase("blue")) {
                                Log.d(TAG, "Blue pin");
                                setPin(PIN_LED_BLUE);
                            } else if (data[1].equalsIgnoreCase("white")) {
                                Log.d(TAG, "White pin");
                                setPin(PIN_LED_WHITE);
                            }
                        } else if (txt.trim().startsWith("BEEP")) {
                            Log.d(TAG, "BEEZER COMMAND");
                            setPin(PIN_BEEZER);
                        }
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });

    }

    private void initPins() {
        try {
            mGpioBlue = service.openGpio(PIN_LED_BLUE);
            mGpioWhite = service.openGpio(PIN_LED_WHITE);
            mGpioBeezer = service.openGpio(PIN_BEEZER);
            mGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mGpioWhite.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mGpioBeezer.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setPin(String pinLed) {
        try {
            switch (pinLed) {
                case PIN_LED_BLUE:
                    situation[0] = !situation[0];
                    Log.e(TAG, "SET" + PIN_LED_BLUE + situation[0]);
                    mGpioBlue.setValue(situation[0]);
                    break;
                case PIN_LED_WHITE:
                    situation[1] = !situation[1];
                    Log.e(TAG, "SET" + PIN_LED_WHITE + situation[1]);
                    mGpioWhite.setValue(situation[1]);
                    break;
                case PIN_BEEZER:
                    situation[2] = !situation[2];
                    Log.e(TAG, "SET" + PIN_BEEZER + situation[2]);
                    mGpioBeezer.setValue(situation[2]);
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGpioBlue != null) {
            try {
                mGpioBlue.close();
                mGpioWhite.close();
                mGpioBeezer.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }


    }


}
