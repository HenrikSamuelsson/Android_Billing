package com.samdide.android.androidbilling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.samdide.android.inappbilling.util.IabHelper;
import com.samdide.android.inappbilling.util.IabResult;
import com.samdide.android.inappbilling.util.Inventory;
import com.samdide.android.inappbilling.util.Purchase;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "com.samdide.android.androidbilling";
    IabHelper mHelper;
    static final String ITEM_SKU = "com.example.buttonclick";

    private Button clickButton;
    private Button buyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onStart() {
        super.onStart();
        buyButton = (Button) findViewById(R.id.buyButton);
        clickButton = (Button) findViewById(R.id.clickButton);
        clickButton.setEnabled(false);

        String base64EncodedPublicKey = getString(R.string.RSA_public_key);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing setup success.");
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Error handling here
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                buyButton.setEnabled(false);
            }
        }
    };

    public void clickButtonClicked(View view) {
        clickButton.setEnabled(false);
        buyButton.setEnabled(true);
    }

    public void buyButtonClicked(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener,
                "mypurchasetoken");
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        clickButton.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };
}
