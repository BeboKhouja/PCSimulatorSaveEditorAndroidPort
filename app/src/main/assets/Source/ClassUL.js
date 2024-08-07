var toggle = document.getElementsByClassName("caret");
var i;

for (i = 0; i < toggle.length; i++) {
  toggle[i].addEventListener("click", function() {
    this.parentElement.querySelector(".nested").classList.toggle("active");
    this.classList.toggle("caret-down");
  });
}