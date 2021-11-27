$ ->
  ws = new WebSocket $("#gitterific-home").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.responseType
      when "searchResult"
        displaySearchResult(message)
      when "searchResultUpdate"
        updateSearchResult(message)

  $("#searchGitHubForm").submit (event) ->
    event.preventDefault()
    # send the message to watch the stock
    ws.send(JSON.stringify({search_query: $("#searchField").val()}))
    # reset the form
    $("#searchField").val("")

  $('#all-search-results').on 'click', 'a.user-profile-link', (event) ->
    event.preventDefault()
    # alert $(this).text()
    ws.send(JSON.stringify({user_profile: $(this).text()}))
    return

replaceSpaceWithUnderscore = (string) ->
  string.replace(" ", "_")

displaySearchResult = (message) ->
  id_query = replaceSpaceWithUnderscore(message.query)
  singleSearchResult = $("<div>").addClass("single-search-result").prop("id", id_query)
  searchResultHeader = $("<div>").addClass("search-result-header")
  searchResultData = $("<div>").addClass("search-result-data").prop("id", id_query + "-result")
  searchResultHeader.append($("<h2>").text("Search terms: " + message.query))
  for repository in message.repositoryList
    respositoryInfo = $("<div>").addClass("repository-info")

    repositoryDetails = $("<p>").append($("<b>").text("Repository Name: "))
    repositoryLink = $("<a>").text(repository.repositoryName).attr("href", "/repositoryProfile/" + repository.ownerName + "/" + repository.repositoryName)
    repositoryDetails.append(repositoryLink)

    repositoryDetails.append($("<b>").text(" | Owner Name: "))
    userProfileLink = $("<a>").text(repository.ownerName).attr("href", "/user-profile/" + repository.ownerName)
    userProfileLink.addClass("user-profile-link")
    repositoryDetails.append(userProfileLink)

    repositoryDetails.append($("<b>").text(" | Topic List:"))
    for topic in repository.topics
      topicLink = $("<a>").text(topic).attr("href", "/topics/" + topic)
      repositoryDetails.append("  ")
      repositoryDetails.append(topicLink)
    respositoryInfo.append(repositoryDetails)

    searchResultData.append(respositoryInfo)
  singleSearchResult.append(searchResultHeader)
  singleSearchResult.append(searchResultData)
  singleSearchResult.append($("<hr>"))
  $("#all-search-results").prepend(singleSearchResult)

updateSearchResult = (message) ->
  id_query = replaceSpaceWithUnderscore(message.query)
  for repository in message.repositoryList
    respositoryInfo = $("<div>").addClass("repository-info")

    repositoryDetails = $("<p>").append($("<b>").text("***New*** Repository Name: "))
    repositoryLink = $("<a>").text(repository.repositoryName).attr("href", "/repositoryProfile/" + repository.ownerName + "/" + repository.repositoryName)
    repositoryDetails.append(repositoryLink)

    repositoryDetails.append($("<b>").text(" | Owner Name: "))
    userProfileLink = $("<a>").text(repository.ownerName).attr("href", "/user-profile/" + repository.ownerName)
    repositoryDetails.append(userProfileLink)

    repositoryDetails.append($("<b>").text(" | Topic List:"))
    for topic in repository.topics
      topicLink = $("<a>").text(topic).attr("href", "/topics/" + topic)
      repositoryDetails.append("  ")
      repositoryDetails.append(topicLink)
    respositoryInfo.append(repositoryDetails)

    $("#" + id_query + "-result").prepend(respositoryInfo)