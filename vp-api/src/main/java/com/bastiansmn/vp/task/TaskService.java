package com.bastiansmn.vp.task;

import com.bastiansmn.vp.exception.FunctionalException;
import com.bastiansmn.vp.task.dto.TaskCreationDTO;

import java.util.Collection;

public interface TaskService {

    TaskDAO create(TaskCreationDTO taskCreationDTO) throws FunctionalException;

    TaskDAO fetchById(Long task_id) throws FunctionalException;

    Collection<TaskDAO> fetchAllOfProject(Long project_id) throws FunctionalException;

    Collection<TaskDAO> fetchAllOfGoal(Long goal_id) throws FunctionalException;

    void delete(Long task_id) throws FunctionalException;

}
