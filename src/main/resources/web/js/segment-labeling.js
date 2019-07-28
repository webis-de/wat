var segmentLabelingLastSegment = -1;

function setSegmentLabelingValue(component, key, value) {
  var segmentId = parseInt(key, 10);
  segmentLabelingLastSegment = segmentId;
  segmentLabelingUpdateClass(component, segmentId, value);
  segmentLabelingUpdateContinued(component, segmentId, value);
  segmentLabelingUpdateCounter(component); 
  segmentLabelingUpdateLastSelected(value); 
}

function segmentLabelingGetSegment(component, id) {
  return $("#" + component + id);
}

function segmentLabelingUpdateClass(component, segmentId, selectedType) {
  var segment = segmentLabelingGetSegment(component, segmentId);
  segment.attr("class", "segment");
  segment.addClass(selectedType);
  segment.attr("data-label", selectedType);
}

function segmentLabelingUpdateContinued(component, segmentId, selectedType) {
  var prevSegment = segmentLabelingGetSegment(component, segmentId - 1)

  if (selectedType != "continued") {
    // THIS.value
    if (prevSegment.size() > 0 && prevSegment.hasClass("continued")) {
      // PREV.continued THIS.value
      segmentLabelingUpdateClass(component, segmentId - 1, "continued");
      prevSegment.addClass(selectedType);
      // -> PREV.continued.value THIS.value
      segmentLabelingUpdateContinued(component, segmentId - 1, selectedType);
    }
  } else {
    // THIS.continued
    var nextSegment = segmentLabelingGetSegment(component, segmentId + 1);

    if (!nextSegment.hasClass("unannotated")) {
      // THIS.continued NEXT[.continued].value
      var nextSelectedType = "";
      $.each(nextSegment.attr('class').split(/\s+/), function(index, className) {
        if (className != "segment" && className != "continued") {
          nextSelectedType = className;
        }
      });
      segmentLabelingGetSegment(component, segmentId).addClass(nextSelectedType);
      // -> THIS.continued.value NEXT[.continued].value
      segmentLabelingUpdateContinued(component, segmentId, nextSelectedType);
    } else if (prevSegment.size() > 0 && prevSegment.hasClass("continued")) {
      // PREV.continued[.value'] THIS.continued NEXT.unannotated
      segmentLabelingUpdateClass(component, segmentId - 1, "continued");
      // -> PREV.continued THIS.continued NEXT.unannotated
      segmentLabelingUpdateContinued(component, segmentId - 1, "continued");
    }
  }

  segmentLabelingUpdateVisuals(component, segmentLabelingGetSegment(component, segmentId));
}

function segmentLabelingUpdateCounter(component) {
  var inputs = $('#' + component + '-body .segment.unannotated');
  var unannotated = inputs.size();

  var counter = $('#' + component + '-state');
  if (unannotated == 0) {
    counter.text('complete');
  } else if (unannotated == 1) {
    counter.text(''+unannotated+' segment remaining');
  } else {
    counter.text(''+unannotated+' segments remaining');
  }
}

function segmentLabelingUpdateVisuals(component, segment) {
  var label = segment.attr('data-label');
  var img = segment.find('img');
  img.attr('src', "../img/" + component + "/" + label + ".png");
}

function segmentLabelingUpdateLastSelected(value) {
  var l;

  var previouslyLastSelected = document.querySelectorAll(".last-selected");
  for (l = 0; l < previouslyLastSelected.length; ++l) {
    previouslyLastSelected[l].classList.remove("last-selected");
  }

  var nowLastSelected = document.querySelectorAll("li." + value);
  for (l = 0; l < nowLastSelected.length; ++l) {
    nowLastSelected[l].classList.add("last-selected");
  }
}

function segmentLabelingToggleSubMenues(label, menuwidth) {
  $("ul[data-submenu]").each(function(index, submenu) {
    if (submenu.getAttribute("data-submenu") != label) {
      $(submenu).parent().removeClass("open");
    }
  });
  $("ul[data-submenu='" + label + "']").each(function(index, submenu) {
    var parentMenuItem = $(submenu).parent();
    var button = parentMenuItem.parent().parent();
    if (button.offset().left + 2 * menuwidth > document.body.clientWidth) {
      parentMenuItem.addClass("dropdown-submenu-left");
    } else {
      parentMenuItem.removeClass("dropdown-submenu-left");
    }
    parentMenuItem.toggleClass("open");
  });
}

function segmentLabelingTabToNextSegment(event) {
  var keyCode = event.keyCode ? event.keyCode : event.which;
  if (keyCode == 9) { // Tab => open next menu
    var dropdown = document.querySelector("#segment-labeling" + (segmentLabelingLastSegment + 1) + " span.dropdown");
    if (dropdown != null) {
      dropdown.classList.add("open");
    }
    event.preventDefault();
  }
}

function segmentLabelingKeyPressAnnotation(event) {
  var keyCode = event.keyCode ? event.keyCode : event.which;
  if (keyCode == 13) { // Enter => use last selected
    var link = document.querySelector(
        "span.open .open > .dropdown-menu > li.last-selected > .annotation-button, " + // open sub menu
        "span.open > .dropdown-menu > li.last-selected > .annotation-button"); // open menu
    if (link != null) {
      link.click();
    }
  } else { // use shortcuts defined in segment-labeling.conf
    var key = String.fromCharCode(keyCode);
    var link = document.querySelector(
        "span.open .open > .dropdown-menu > li > .annotation-button[data-key='" + key + "'], " + // open sub menu
        "span.open > .dropdown-menu > li > .annotation-button[data-key='" + key + "']"); // open menu
    if (link != null) {
      link.click();
    }
  }
}

// Will be called when page is loaded using the WAT jsInitFunction feature
function segmentLabelingInitialize(component) {
  segmentLabelingUpdateCounter(component);
  $('#' + component + '-body .segment').each(function() {
    $(this).find('img').attr('src', '../img/dropdown.png');
    segmentLabelingUpdateVisuals(component, $(this));
  });
  // Submenu adapted from https://www.w3schools.com/bootstrap/tryit.asp?filename=trybs_ref_js_dropdown_multilevel_css&stacked=h
  $('.dropdown-submenu a.dropdown-submenu-toggle').on('click', function(e) {
    segmentLabelingToggleSubMenues(e.target.getAttribute("data-submenu"), $(this).parent().width());
    e.stopPropagation();
    e.preventDefault();
  });
  document.addEventListener("keydown", segmentLabelingTabToNextSegment);
  document.addEventListener("keypress", segmentLabelingKeyPressAnnotation);
}
