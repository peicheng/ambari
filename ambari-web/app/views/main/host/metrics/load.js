/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var App = require('app');

/**
 * @class
 * 
 * This is a view for showing host load
 * 
 * @extends App.ChartLinearTimeView
 * @extends Ember.Object
 * @extends Ember.View
 */
App.ChartHostMetricsLoad = App.ChartLinearTimeView.extend({
  id: "host-metrics-load",
  title: Em.I18n.t('hosts.host.metrics.load'),
  renderer: 'line',
  sourceUrl: "/hosts/{hostName}?fields=metrics/load/load_fifteen[{fromSeconds},{toSeconds},{stepSeconds}],metrics/load/load_one[{fromSeconds},{toSeconds},{stepSeconds}],metrics/load/load_five[{fromSeconds},{toSeconds},{stepSeconds}]",
  mockUrl: "/data/hosts/metrics/load.json",

  transformToSeries: function (jsonData) {
    var seriesArray = [];
    if (jsonData && jsonData.metrics && jsonData.metrics.load) {
      for ( var name in jsonData.metrics.load) {
        var displayName;
        var seriesData = jsonData.metrics.load[name];
        switch (name) {
          case "load_fifteen":
            displayName = Em.I18n.t('hosts.host.metrics.load.displayNames.load_fifteen');
            break;
          case "load_one":
            displayName = Em.I18n.t('hosts.host.metrics.load.displayNames.load_one');
            break;
          case "load_five":
            displayName = Em.I18n.t('hosts.host.metrics.load.displayNames.load_five');
            break;
          default:
            break;
        }
        if (seriesData) {
          seriesArray.push(this.transformData(seriesData, displayName));
        }
      }
    }
    return seriesArray;
  }
});