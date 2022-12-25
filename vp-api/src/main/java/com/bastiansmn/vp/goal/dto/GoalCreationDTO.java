package com.bastiansmn.vp.goal.dto;

import com.bastiansmn.vp.goal.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class GoalCreationDTO {

    private String name;
    private String description;
    private Date date_start;
    private GoalStatus goalStatus;
    private Date deadline;
    private String project_id;

}
