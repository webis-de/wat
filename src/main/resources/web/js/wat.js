function getTimezoneOffset() {
  return new Date().getTimezoneOffset();
}

// Initializing logout and task selection buttons
$(document).ready(function() {
  $(".button-logout").click(function(e) {
    if (confirm("You really want to log out?")) {
      $("#task-internal-field-annotator").val("");
      $("#task-internal-field-password").val("");
      $("#form").submit();
    }
  });
  $("input[name='timezone']").val(getTimezoneOffset());
});
