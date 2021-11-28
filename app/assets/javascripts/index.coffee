printRepositoryDetails = (objectValue) ->
        for key,value of objectValue
            $('#time').append "<b>" + key + "</b>: " + value + "<br/>"

$ ->
  ws = new WebSocket $("body").data("ws-url")
  issueLink = $("body").data("issueLink")

  ws.onmessage = (event) ->
    message = JSON.parse event.data

    for key,value of message.repositoryProfile
        if(typeof value == "object")
            printRepositoryDetails value
        else
            $('#time').append "<b>" + key + "</b>: " + value + "<br/>"

    $('#time').append "<br><b><h3>List of Issues:</h3></b>"
    for k,v of message.issueList
          index = parseInt(k) + 1
          $('#time').append "<b>" + index + " -</b><a href=" + issueLink + ">" + v + "</a><br/>"


