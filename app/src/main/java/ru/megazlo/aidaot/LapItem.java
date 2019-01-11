package ru.megazlo.aidaot;

import org.joda.time.LocalTime;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Created by iGurkin on 23.11.2018. */
@Getter
@Setter
@Accessors(chain = true)
public class LapItem implements Serializable {

	private boolean started;

	private boolean afterOt;

	private int position;

	private LocalTime time;

	private LocalTime alarmTime;
}
