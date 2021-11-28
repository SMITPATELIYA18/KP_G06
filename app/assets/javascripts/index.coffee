$ ->
  ws = new WebSocket $("#gitterific-home").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.responseType
      when "searchResult"
        $("#user-profile-info").hide()
        displaySearchResult(message)
        $("#all-search-results").show()
      when "searchResultUpdate"
        $("#user-profile-info").hide()
        updateSearchResult(message)
        $("#all-search-results").show()
      when "searchResultPeriodicUpdate"
        updateSearchResult(message)
      when "userProfileInfo"
        $("#all-search-results").hide()
        displayUserProfileInfo(message)
        $("#user-profile-info").show()

  $("#searchGitHubForm").submit (event) ->
    event.preventDefault()
    x = $("#searchField").val()
    if x == ''
      alert 'Please enter a string to be searched.'
      return false
    else
      x = x.toLowerCase()
      ws.send(JSON.stringify({search_query: x}))
      # reset the form
      $("#searchField").val("")
      return

  $("#all-search-results").on "click", "a.user-profile-link", (event) ->
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
    userProfileLink.addClass("user-profile-link")
    repositoryDetails.append(userProfileLink)

    repositoryDetails.append($("<b>").text(" | Topic List:"))
    for topic in repository.topics
      topicLink = $("<a>").text(topic).attr("href", "/topics/" + topic)
      repositoryDetails.append("  ")
      repositoryDetails.append(topicLink)
    respositoryInfo.append(repositoryDetails)

    $("#" + id_query + "-result").prepend(respositoryInfo)

displayUserProfileInfo = (message) ->
  $("#user-profile-info").empty()
  username = message.profile.login
  $("#user-profile-info").append($("<h3>").text("User profile information for: " + username))
  $("#user-profile-info").append($("<br>"))
  userProfileData = $("<div>").prop("id", "user-info")
  userProfileElements = $("<ul>")
  $.each message.profile, (key, value) ->
    userProfileElement = $("<li>")
    userProfileElement.append($("<b>").text(key))
    userProfileElement.append($("<b>").text(": "))
    userProfileElement.append(value)
    userProfileElements.append(userProfileElement)
  userProfileData.append(userProfileElements)
  $("#user-profile-info").append(userProfileData)
  $("#user-profile-info").append($("<hr>"))
  $("#user-profile-info").append($("<h3>").text(username + "'s Repositories:"))
  userRepos = $("<div>").prop("id", "user-repositories")
  if Array.isArray(message.repositories)
    if message.repositories.length > 0
      repoList = $("<p>").text(message.repositories.length + " repositories available for this user.")
      for repo in message.repositories
        repoEntry = $("<li>")
        repoEntryLink = $("<a>").text(repo).attr("href", "/repositoryProfile/" + username + "/" + repo)
        repoEntryLink.addClass("repo-profile-link")
        repoEntry.append(repoEntryLink)
        repoList.append(repoEntry)
      userRepos.append(repoList)
    else
      userRepos.append($("<p>").text("No public repositories available for this user."))
  else
    userRepos.append($("<p>").text("No repositories available for this username."))
  $("#user-profile-info").append(userRepos)
