function setSegmentLabelingValue(component, key, value) {
  var segmentId = parseInt(key, 10);
  segmentLabelingUpdateClass(component, segmentId, value);
  segmentLabelingUpdateContinued(component, segmentId, value);
  segmentLabelingUpdateCounter(component); 
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
  img.attr('title', label);
}

// Will be called when page is loaded using the WAT jsInitFunction feature
function segmentLabelingInitialize(component) {
  segmentLabelingUpdateCounter(component);
  $('#' + component + '-body .segment').each(function() {
    $(this).find('img').attr('src', '../img/dropdown.png');
    segmentLabelingUpdateVisuals(component, $(this));
  });
}
