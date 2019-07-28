// Method to send updates to server
function update(component, key, value) {
  var annotator = $("#task-internal-field-annotator").val();
  var password = $("#task-internal-field-password").val();
  var task = $("#task-internal-field-task").val();
  var isAdminLogin = $("#task-internal-field-is-admin-login").val();
  var url = "update"
      + "?annotator="+escape(annotator)
      + "&password="+escape(password)
      + "&task="+escape(task)
  		+ "&component="+escape(component)
  		+ "&key="+escape(key)
  		+ "&value="+escape(value)
      + "&timezone="+getTimezoneOffset()
      + "&is-admin-login="+escape(isAdminLogin);

  $.getJSON(url, function(data) {
    setValue(component, key, value);
    setComplete(data.complete);
  }).fail(function () {
	  alert("The annotation server is currently not responding. " +
	  		"Please try to log out and in again. " +
	  		"If the problem persists, please contact us and try again later.");
  });

}

// Method to display boxes when task is complete
function setComplete(isComplete) {
  if (isComplete) {
    $(".is-complete-box").show(1000);
  } else {
    $(".is-complete-box").hide(1000);
  }
}

// Initializing task selection buttons
$(document).ready(function() {
  $(".task-selection").click(function(e) {
    $("#task-internal-field-task").val("");
    $("#form").submit();
  });
});

var setValueFunctions = {};

function setValue(component, key, value) {
	setValueFunctions[component](component, key, value);
}
