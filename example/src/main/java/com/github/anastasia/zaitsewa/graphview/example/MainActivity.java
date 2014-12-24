package com.github.anastasia.zaitsewa.graphview.example;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.github.anastasia.zaitsewa.graphview.GraphView;
import com.github.anastasia.zaitsewa.graphview.PlotStyle;
import com.github.anastasia.zaitsewa.graphview.Point;
import com.github.anastasia.zaitsewa.graphview.PointsProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GraphView graphView = (GraphView) findViewById(R.id.graphView);
        graphView.addPlot(
                new ExamplePointsProvider(),
                new PlotStyle()
        );
    }

    private static class ExamplePointsProvider implements PointsProvider {

        private final Observable observable = new PointsObservable();

        @Override
        public List<Point> getPoints() {
            return Arrays.asList(
                    new Point(0, 0),
                    new Point(5, 4),
                    new Point(8, 10),
                    new Point(10, 10),
                    new Point(12, 12)
            );
        }

        @Override
        public String getLabelX(double x) {
            return String.valueOf(x);
        }

        @Override
        public String getLabelY(double y) {
            return String.valueOf(y);
        }

        @Override
        public double getScaleStepX() {
            return 5;
        }

        @Override
        public double getScaleStepY() {
            return 5;
        }

        @Override
        public void addObserver(Observer observer) {
            observable.addObserver(observer);
            observable.notifyObservers();
        }

        private class PointsObservable extends Observable {

            @Override
            public void notifyObservers() {
                setChanged();
                super.notifyObservers();
            }
        }

    }

}
