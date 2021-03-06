/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.agent;



public class ComponentStatus {
  String componentName;
  String msg;
  String status;
  String serviceName;
  String clusterName;
  String stackVersion;

  public String getComponentName() {
    return this.componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getMessage() {
    return this.msg;
  }

  public void setMessage(String msg) {
    this.msg = msg;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStackVersion() {
    return this.stackVersion;
  }

  public void setStackVersion(String stackVersion) {
    this.stackVersion = stackVersion;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  @Override
  public String toString() {
    return "ComponentStatus{" +
            "componentName='" + componentName + '\'' +
            ", msg='" + msg + '\'' +
            ", status='" + status + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", clusterName='" + clusterName + '\'' +
            ", stackVersion='" + stackVersion + '\'' +
            '}';
  }
}
