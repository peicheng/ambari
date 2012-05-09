InstallationWizard.SelectServices = {

  renderData: {},

  render:
    function (selectServicesInfo) {

      InstallationWizard.SelectServices.renderData = selectServicesInfo;

    }
};

var data;

function renderServiceList(responseJson) {
  data = responseJson;
  var divContent = '';
  var coreContent = '';
  var optionalContent = '';
  var nonSelectableContent = '';
  for (serviceName in data['services'])  {
    data['services'][serviceName]['refCount'] = 0;
    if (data['services'][serviceName]['reverseDependencies'] == null) {
      data['services'][serviceName]['reverseDependencies'] = new Array();
    }
    for (var i = 0; i < data['services'][serviceName]['dependencies'].length; i++) {
      svcDep = data['services'][serviceName]['dependencies'][i];
      if (data['services'][svcDep]['reverseDependencies'] == null) {
        data['services'][svcDep]['reverseDependencies'] = new Array();
      }
      var found = false;
      for (var j = 0; j < data['services'][svcDep]['reverseDependencies'].length; j++) {
        if (data['services'][svcDep]['reverseDependencies'][j] == serviceName) {
          found = true;
          break;
        }
      }
      if (!found) {
        data['services'][svcDep]['reverseDependencies'].push(serviceName);
      }
    }

    // globalYui.log("Handling service : " + serviceName);
    var content = '';
    content = generateSelectServiceCheckbox(data['services'][serviceName]);

    if (data['services'][serviceName].attributes.mustInstall) {
      coreContent += content;
    } else {
      if (data['services'][serviceName].attributes.editable) {
        optionalContent += content;
      }
      else {
        nonSelectableContent += content;
      }
    } 

  } 

  //            divContent += coreContent + optionalContent + nonSelectableContent;
  globalYui.one("#selectCoreServicesDynamicRenderDivId").setContent(coreContent);     
  globalYui.one("#selectOptionalServicesDynamicRenderDivId").setContent(optionalContent);
  globalYui.one("#selectNonSelectableServicesDynamicRenderDivId").setContent(nonSelectableContent);
  globalYui.one('#selectServicesCoreDivId').setStyle("display", "block");
}


function generateSelectServiceCheckbox(serviceInfo) {

  var dContent = '<div class="formElement" name="' + serviceInfo.serviceName + '" id="'
      + 'selectServicesEntry' + serviceInfo.serviceName + 'DivId"';

  if (!serviceInfo.attributes.editable
      && !serviceInfo.attributes.mustInstall) {
    dContent += ' style="display:none" ';
  }
  dContent += '><label for="install' + serviceInfo.serviceName + 'Id"'
      + '>' + serviceInfo.displayName + '</label>'
      + '<input type="checkbox" name="' + serviceInfo.serviceName + '"'
      + ' id="installService' + serviceInfo.serviceName + 'Id" value="install'
      + serviceInfo.serviceName + 'Value"';

  if (serviceInfo.attributes != null) {
     if (serviceInfo.attributes.noDisplay) {
        return '';
     }
     if (!serviceInfo.attributes.editable) {
       dContent += ' disabled="disabled"';
     }
     if (serviceInfo.attributes.mustInstall) {
       dContent += ' checked="yes"';
     }
  }
 
  dContent += '/>' +
          '<div class="contextualHelp">' + serviceInfo['description'] + '</div>' +
        '</div>';

  // globalYui.log("Handling service entry: " + dContent);
  return dContent;
}

globalYui.one('#selectServicesCoreDivId').delegate('click', function (e) {
    // globalYui.log(globalYui.Lang.dump(this));
    var serviceName = this.getAttribute('name');
    var buttonId = 'installService' + serviceName + 'Id';

    // Deselecting an already selected service
    if (!globalYui.one('#' + buttonId).get('checked')) {
      var invalidDep = false;
      var invalidDepReason = "";
      for (var i = 0; i < data['services'][serviceName]['reverseDependencies'].length; i++) {
        var nm = data['services'][serviceName]['reverseDependencies'][i];
        if (data['services'][nm]['refCount'] > 0) {
          invalidDep = true;
          invalidDepReason = "Cannot deselect: " + data['services'][serviceName].displayName + " is needed by " + data['services'][nm].displayName;
          break;
        }
      }
      if (invalidDep) {
        setFormStatus(invalidDepReason, true);
        globalYui.one('#' + buttonId).set('checked', 'yes');
        return;
      }
    }

    var selectYes = true;
    var statusString = "Selected " + data['services'][serviceName].displayName + " for installation. ";
    if (!globalYui.one('#' + buttonId).get('checked')) {
       selectYes = false;
       statusString = "Deselected " + data['services'][serviceName].displayName + " and all its dependencies.";
       data['services'][serviceName]['refCount'] = 0;
    } else if (data['services'][serviceName]['refCount'] == 0) {
       data['services'][serviceName]['refCount'] = 1;
    }

    // globalYui.log('selectYes: ' + selectYes);
    // globalYui.log('serviceName: ' + serviceName);

    var dependencies = "";
    for (var i = 0; i < data['services'][serviceName]['dependencies'].length; i++) {
       var serviceDep = data['services'][serviceName]['dependencies'][i];
       if (selectYes) {
          data['services'][serviceDep]['refCount']++;
          if (!data['services'][serviceDep].attributes.mustInstall) {
            dependencies += data['services'][serviceDep].displayName + " ";
          }
       } else {
          data['services'][serviceDep]['refCount']--;
          if (data['services'][serviceDep]['refCount'] < 0) {
             data['services'][serviceDep]['refCount'] = 0;
          }
       }
    }
    if(selectYes) {
      if(dependencies != "") {
        statusString += "Also added  " + dependencies + " as dependencies.";
      }
    }
    setFormStatus(statusString, false);

    for (svcName in data['services']) {
       if (data['services'][svcName].attributes.noDisplay) {
         continue;
       }

       // globalYui.log('Svc ref count : ' + svcName + ' : ' + data['services'][svcName]['refCount']);
     
       var itemId = 'installService' + svcName + 'Id';
       if (data['services'][svcName].attributes.mustInstall ||
           data['services'][svcName]['refCount'] > 0) {
          globalYui.one('#' + itemId).set('checked' ,'yes');
          if (!data['services'][svcName].attributes.editable) {
             var divId = 'selectServicesEntry' + svcName + 'DivId';
             globalYui.one('#' + divId).setStyle('display', '');
          }
       } else {
          globalYui.one('#' + itemId).set('checked' ,'');
          if (!data['services'][svcName].attributes.editable) {
             var divId = 'selectServicesEntry' + svcName + 'DivId';
             globalYui.one('#' + divId).setStyle('display', 'none');
          }
       }
    }

//}, 'li.selectServicesEntry');
}, 'input[type=checkbox]');

globalYui.one('#selectServicesSubmitButtonId').on('click',function (e) {
    var selectServicesRequestData = {
        "services" : [ ] } ;
    for (svcName in data['services']) {
       /* if (data['services'][svcName].attributes.noDisplay) {
         continue;
       }*/
       var svcObj = { "serviceName" : svcName,
                      "isEnabled": (data['services'][svcName].attributes.mustInstall || data['services'][svcName]['refCount'] > 0) };
       selectServicesRequestData.services.push(svcObj);
    }

    // alert(globalYui.Lang.dump(selectServicesRequestData));

    var url = "../php/frontend/selectServices.php?clusterName=" + InstallationWizard.SelectServices.renderData.clusterName;
    var requestData = selectServicesRequestData;
    var submitButton = globalYui.one('#selectServicesSubmitButtonId');
    var thisScreenId = "#selectServicesCoreDivId";
    var nextScreenId  = "#assignHostsCoreDivId";
    var nextScreenRenderFunction = renderAssignHosts;

    submitDataAndProgressToNextScreen(url, requestData, submitButton, thisScreenId, nextScreenId, nextScreenRenderFunction);

});

function renderSelectServicesBlock(infoInitializeCluster) {

  InstallationWizard.SelectServices.renderData = infoInitializeCluster;

  //////// Get the list of services and relevant information for rendering them.
  var clusterName = InstallationWizard.SelectServices.renderData.clusterName;
  var inputUrl = "../php/frontend/fetchClusterServices.php?clusterName=" + clusterName ;
  executeStage(inputUrl, renderServiceList);
}