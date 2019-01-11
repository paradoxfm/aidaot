package ru.megazlo.aidaot.dto;

import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import ru.megazlo.aidaot.LapItem;

/** Created by iGurkin on 26.11.2018. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LapsState implements Serializable {

	private int lapTime;

	private LocalTime start;

	private List<LapItem> laps = new ArrayList<>();
}
