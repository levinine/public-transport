package com.factory.problem.state;

import com.factory.model.Line;
import com.factory.model.Stop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PassengerState implements Cloneable {

    private String lat;

    private String lon;

    private double timeElapsed = 0;

    private Stop station;

    private Line line;

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getTimeElapsedInMillis() {
        return Math.round(timeElapsed * 3600000);
    }
}
