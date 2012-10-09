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

package org.apache.ambari.server.controller;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.actionmanager.ActionDBAccessor;
import org.apache.ambari.server.actionmanager.ActionDBInMemoryImpl;
import org.apache.ambari.server.actionmanager.ActionManager;
import org.apache.ambari.server.actionmanager.Stage;
import org.apache.ambari.server.agent.ActionQueue;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.State;
import org.apache.ambari.server.state.cluster.ClustersImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmbariManagementControllerTest {

  private static final Logger LOG =
      LoggerFactory.getLogger(AmbariManagementControllerTest.class);

  private AmbariManagementController controller;
  private Clusters clusters;
  private ActionDBAccessor db = new ActionDBInMemoryImpl();

  @Before
  public void setup() {
    clusters = new ClustersImpl();
    ActionManager am = new ActionManager(5000, 1200000, new ActionQueue(),
        clusters, db);
    controller = new AmbariManagementControllerImpl(am, clusters);
  }

  @After
  public void teardown() {
    controller = null;
    clusters = null;
  }

  private void createCluster(String clusterName) throws AmbariException {
    ClusterRequest r = new ClusterRequest(null, clusterName, "1.0.0", null);
    controller.createCluster(r);

    try {
      controller.createCluster(r);
      fail("Duplicate cluster creation should fail");
    } catch (Exception e) {
      // Expected
    }
  }

  private void createService(String clusterName,
      String serviceName, State desiredState) throws AmbariException {
    String dStateStr = null;
    if (desiredState != null) {
      dStateStr = desiredState.toString();
    }
    ServiceRequest r = new ServiceRequest(clusterName, serviceName, null,
        dStateStr);
    controller.createService(r);

    try {
      controller.createService(r);
      fail("Duplicate Service creation should fail");
    } catch (Exception e) {
      // Expected
    }
  }

  private void createServiceComponent(String clusterName,
      String serviceName, String componentName, State desiredState)
          throws AmbariException {
    String dStateStr = null;
    if (desiredState != null) {
      dStateStr = desiredState.toString();
    }
    ServiceComponentRequest r = new ServiceComponentRequest(clusterName,
        serviceName, componentName, null, dStateStr);
    controller.createComponent(r);

    try {
      controller.createComponent(r);
      fail("Duplicate ServiceComponent creation should fail");
    } catch (Exception e) {
      // Expected
    }
  }

  private void createServiceComponentHost(String clusterName,
      String serviceName, String componentName, String hostname,
      State desiredState) throws AmbariException {
    String dStateStr = null;
    if (desiredState != null) {
      dStateStr = desiredState.toString();
    }
    ServiceComponentHostRequest r = new ServiceComponentHostRequest(clusterName,
        serviceName, componentName, hostname, null, dStateStr);
    controller.createHostComponent(r);

    try {
      controller.createHostComponent(r);
      fail("Duplicate ServiceComponentHost creation should fail");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testCreateCluster() throws AmbariException {
    String clusterName = "foo1";
    createCluster(clusterName);
    Set<ClusterResponse> r =
        controller.getClusters(new ClusterRequest(null, clusterName, null, null));
    Assert.assertEquals(1, r.size());
    ClusterResponse c = r.iterator().next();
    Assert.assertEquals(clusterName, c.getClusterName());
  }

  @Test
  public void testCreateService() throws AmbariException {
    String clusterName = "foo1";
    createCluster(clusterName);
    String serviceName = "HDFS";
    try {
      createService(clusterName, serviceName, State.INSTALLING);
      fail("Service creation should fail for invalid state");
    } catch (Exception e) {
      // Expected
    }
    try {
      clusters.getCluster(clusterName).getService(serviceName);
      fail("Service creation should have failed");
    } catch (Exception e) {
      // Expected
    }
    try {
      createService(clusterName, serviceName, State.INSTALLED);
      fail("Service creation should fail for invalid initial state");
    } catch (Exception e) {
      // Expected
    }

    createService(clusterName, serviceName, null);

    String serviceName2 = "MAPREDUCE";
    createService(clusterName, serviceName2, State.INIT);

    ServiceRequest r = new ServiceRequest(clusterName, null, null, null);
    Set<ServiceResponse> response = controller.getServices(r);
    Assert.assertEquals(2, response.size());

    for (ServiceResponse svc : response) {
      Assert.assertTrue(svc.getServiceName().equals(serviceName)
          || svc.getServiceName().equals(serviceName2));
      Assert.assertEquals("1.0.0", svc.getDesiredStackVersion());
      Assert.assertEquals(State.INIT.toString(), svc.getDesiredState());
    }


  }

  @Test
  public void testCreateServiceComponent() throws AmbariException {
    String clusterName = "foo1";
    createCluster(clusterName);
    String serviceName = "HDFS";
    createService(clusterName, serviceName, null);

    String componentName = "NAMENODE";
    try {
      createServiceComponent(clusterName, serviceName, componentName,
          State.INSTALLING);
      fail("ServiceComponent creation should fail for invalid state");
    } catch (Exception e) {
      // Expected
    }
    try {
      clusters.getCluster(clusterName).getService(serviceName)
          .getServiceComponent(componentName);
      fail("ServiceComponent creation should have failed");
    } catch (Exception e) {
      // Expected
    }

    createServiceComponent(clusterName, serviceName, componentName,
        State.INIT);
    Assert.assertNotNull(clusters.getCluster(clusterName)
        .getService(serviceName).getServiceComponent(componentName));

    ServiceComponentRequest r =
        new ServiceComponentRequest(clusterName, serviceName, null, null, null);
    Set<ServiceComponentResponse> response = controller.getComponents(r);
    Assert.assertEquals(1, response.size());

    ServiceComponentResponse sc = response.iterator().next();
    Assert.assertEquals(State.INIT.toString(), sc.getDesiredState());
    Assert.assertEquals(componentName, sc.getComponentName());
    Assert.assertEquals(clusterName, sc.getClusterName());
    Assert.assertEquals(serviceName, sc.getServiceName());
  }

  @Test
  public void testCreateServiceComponentHost() throws AmbariException {
    String clusterName = "foo1";
    createCluster(clusterName);
    String serviceName = "HDFS";
    createService(clusterName, serviceName, null);
    String componentName1 = "NAMENODE";
    String componentName2 = "DATANODE";
    createServiceComponent(clusterName, serviceName, componentName1,
        State.INIT);
    createServiceComponent(clusterName, serviceName, componentName2,
        State.INIT);

    String host1 = "h1";
    String host2 = "h2";

    try {
      createServiceComponentHost(clusterName, serviceName, componentName1,
          host1, State.INSTALLING);
      fail("ServiceComponentHost creation should fail for invalid state");
    } catch (Exception e) {
      // Expected
    }

    try {
      clusters.getCluster(clusterName).getService(serviceName)
          .getServiceComponent(componentName1).getServiceComponentHost(host1);
      fail("ServiceComponentHost creation should have failed");
    } catch (Exception e) {
      // Expected
    }

    createServiceComponentHost(clusterName, serviceName, componentName1,
        host1, null);
    createServiceComponentHost(clusterName, serviceName, componentName2,
        host1, null);
    createServiceComponentHost(clusterName, serviceName, componentName2,
        host2, null);

    try {
      createServiceComponentHost(clusterName, serviceName, componentName1,
          host1, null);
      fail("ServiceComponentHost creation should fail as duplicate");
    } catch (Exception e) {
      // Expected
    }

    Assert.assertNotNull(clusters.getCluster(clusterName)
        .getService(serviceName)
        .getServiceComponent(componentName1)
        .getServiceComponentHost(host1));
    Assert.assertNotNull(clusters.getCluster(clusterName)
        .getService(serviceName)
        .getServiceComponent(componentName2)
        .getServiceComponentHost(host1));
    Assert.assertNotNull(clusters.getCluster(clusterName)
        .getService(serviceName)
        .getServiceComponent(componentName2)
        .getServiceComponentHost(host2));

    ServiceComponentHostRequest r =
        new ServiceComponentHostRequest(clusterName, serviceName,
            componentName2, null, null, null);

    Set<ServiceComponentHostResponse> response =
        controller.getHostComponents(r);
    Assert.assertEquals(2, response.size());

    // TODO fix
  }

  @Test
  public void testCreateHost() throws AmbariException {

  }

  @Test
  public void testGetCluster() throws AmbariException {

  }

  @Test
  public void testGetService() throws AmbariException {

  }

  @Test
  public void testGetServiceComponent() throws AmbariException {

  }

  @Test
  public void testGetServiceComponentHost() throws AmbariException {

  }

  @Test
  public void testGetHost() throws AmbariException {

  }

  @Test
  public void testInstallAndStartService() throws AmbariException {
    testCreateServiceComponentHost();

    String clusterName = "foo1";
    String serviceName = "HDFS";

    ServiceRequest r1 = new ServiceRequest(clusterName, serviceName, null,
        State.INSTALLED.toString());

    controller.updateService(r1);

    // TODO validate stages?
    List<Stage> stages = db.getAllStages(1);
    Assert.assertEquals(2, stages.size());

    for (Stage stage : stages) {
      LOG.info("Stage Details for Install Service: " + stage);
    }

    ServiceRequest r2 = new ServiceRequest(clusterName, serviceName, null,
        State.STARTED.toString());

    controller.updateService(r2);

    // TODO validate stages?
    stages = db.getAllStages(2);
    Assert.assertEquals(2, stages.size());

    for (Stage stage : stages) {
      LOG.info("Stage Details for Start Service: " + stage);
    }


  }


}