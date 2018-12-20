/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.pelican.deployment.element;

import java.util.List;

/**
 * @author moyun@middleware
 */
public interface Project extends CustomConfiguration {

	void init();

	boolean isActived();

	void setActived(boolean actived);

	int getVersion();

	void addMachine(Machine machine);

	void removeMachine(String id);

	List<Machine> getAllMachines();

	List<String> getAllMachineIps();

	Machine getMachineById(String id);

	List<Machine> getAllMachinesByIds(String... ids);

	List<Machine> getAllMachinesByGroup(String group);

	List<Machine> getAllCustomizedMachines(Class<?> clazz);

	List<Application> getApplicationsByGroup(String group);

	Application getApplicationById(String id);

	String getProjectName();

	String getEnvironmentMode();

	void setEnvironmentMode(String evnMode);

	boolean autoLoginEnable();

	boolean checkLinuxCommandEnable();

	void deploy();

}
