package xyz.bboylin.demo;

import xyz.bboylin.demo.a.Outer;
import xyz.bboylin.demolib.a.OuterInLib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.log("start test inner:");
        new Outer().testInner();
        Utils.log("start test inner static:");
        Outer.testInnerStatic();

        Utils.log("start test jar inner:");
        new OuterInLib().testInner();
        Utils.log("start test jar inner static:");
        OuterInLib.testInnerStatic();

        Utils.log("end");
    }
}
