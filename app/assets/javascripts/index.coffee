$ ->
  # Requests a web socket from the server for two-way fully duplex communication
  ws = new WebSocket $("#gitterific-home").data("ws-url")

  # On receiving a message, checks the response type and renders data accordingly
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.responseType
      when "searchResult"
        $("#repository-profile-info").hide()
        $("#user-profile-info").hide()
        $("#issue-stat-info").hide()
        displaySearchResult(message)
        $("#all-search-results").show()
      when "searchResultUpdate"
        $("#repository-profile-info").hide()
        $("#user-profile-info").hide()
        $("#issue-stat-info").hide()
        updateSearchResult(message)
        $("#all-search-results").show()
      when "searchResultPeriodicUpdate"
        updateSearchResult(message)
      when "userProfileInfo"
        $("#all-search-results").hide()
        displayUserProfileInfo(message)
        $("#user-profile-info").show()
      when "repositoryProfileInfo"
        $("#all-search-results").hide()
        $("#issue-stat-info").hide()
        $("#user-profile-info").hide()
        displayRepositoryProfileInfo(message)
        $("#repository-profile-info").show()
      when "issueStatInfo"
        $("#repository-profile-info").hide()
        $("#user-profile-info").hide()
        $("#all-search-results").hide()
        displayIssueStatInfo(message)
        $("#issue-stat-info").show()

  # When the form button is clicked, validates the input and sends a request using the web socket
  $("#searchGitHubForm").submit (event) ->
    event.preventDefault()
    searchQuery = $("#searchField").val()
    if searchQuery == ""
      alert "Please enter a string to be searched."
      return false
    else
      searchQuery = searchQuery.toLowerCase()
      ws.send(JSON.stringify({search_query: searchQuery}))
      # reset the form
      $("#searchField").val("")
      return

  # When a user profile link is clicked, sends a request to retrieve user profile and repositories information using
  # the web socket
  $("#all-search-results").on "click", "a.user-profile-link", (event) ->
    event.preventDefault()
    ws.send(JSON.stringify({user_profile: $(this).text()}))
    return

  $("#all-search-results").on "click", "a.repository-profile-link", (event) ->
      event.preventDefault()
      ws.send(JSON.stringify({repository_profile: $(this).text(), username: $(this).attr("username")}))
      return

  $("#repository-profile-info").on "click", "a.issue-profile-link", (event) ->
      event.preventDefault()
      ws.send(JSON.stringify({issues: $(this).attr("repoFullName")}))
      return

# Replaces spaces in a string with underscores
replaceSpaceWithUnderscore = (string) ->
  string.replace(" ", "_")

# Displays search results for fresh search results (this function is not for periodic updates to the search results)
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
    repositoryLink.addClass("repository-profile-link")
    repositoryLink.attr("username", repository.ownerName)
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

# Updates displayed search results with new data received from the server
updateSearchResult = (message) ->
  id_query = replaceSpaceWithUnderscore(message.query)
  for repository in message.repositoryList
    respositoryInfo = $("<div>").addClass("repository-info")

    repositoryDetails = $("<p>").append($("<b>").text("***New*** Repository Name: "))
    repositoryLink = $("<a>").text(repository.repositoryName).attr("href", "/repositoryProfile/" + repository.ownerName + "/" + repository.repositoryName)
    repositoryLink.addClass("repository-profile-link")
    repositoryLink.attr("username", repository.ownerName)
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

# Renders user profile and repositories information
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
        repositoryEntry = $("<li>")
        repositoryEntryLink = $("<a>").text(repo).attr("href", "/repositoryProfile/" + username + "/" + repo)
        repositoryEntryLink.addClass("repository-profile-link")
        repositoryEntry.append(repositoryEntryLink)
        repoList.append(repositoryEntry)
      userRepos.append(repoList)
    else
      userRepos.append($("<p>").text("No public repositories available for this user."))
  else
    userRepos.append($("<p>").text("No repositories available for this username."))
  $("#user-profile-info").append(userRepos)


displayRepositoryProfileInfo = (message) ->
  $("#repository-profile-info").empty()
  repositoryName = message.repositoryProfile.name
  username = message.repositoryProfile.owner.login
  for key,value of message.repositoryProfile
    if(typeof value == "object")
      printRepositoryDetails value, repositoryName
    else
      $('#repository-profile-info').append "<b>" + key + "</b>: " + value + "<br/>"

  $('#repository-profile-info').append "<br><b><h3>List of Issues:</h3></b>"
  if message.issueList.length > 0
      for key,value of message.issueList
            index = parseInt(key) + 1
            issueLink = $("<a>").text(value).attr("href", "/issues/" + username + "/" + repositoryName)
            issueLink.addClass("issue-profile-link")
            issueLink.attr("repoFullName", username + "/" + repositoryName)
            $("#repository-profile-info").append( "<b>" + index + " -</b> ").append(issueLink).append("<br>")
  else
      $("#repository-profile-info").append("No issues found")

printRepositoryDetails = (objectValue, repositoryName) ->
        for key,value of objectValue
            if(key == "login")
                  userProfileLink = $("<a>").text(value).attr("href", "/user-profile/" + repositoryName)
                  userProfileLink.addClass("user-profile-link")
                  $("#repository-profile-info").append( "<b>" + key + " :</b> ").append(userProfileLink).append("<br>")
            else
                  $('#repository-profile-info').append "<b>" + key + "</b>: " + value + "<br/>"


displayIssueStatInfo = (issueModel) -> 
    $("#issue-stat-info").empty();
    $("#issue-stat-info").append("<h1>").text("A word-level statistics of the issue titles")
    $("#issue-stat-info").append("<br/>")
    $("#issue-stat-info").append("<span>").append("<i>").text("(by frequency of the words in descending order)")

    repoFullName = issueModel.result.repoFullName
    $("#issue-stat-info").append($("<h2>").text("Repository Name: " + repoFullName))
    $("#issue-stat-info").append($("<br>"))
    issueStat = $("<div>")
    if issueModel.result.error
        $("#issue-stat-info").append("<div><h4>" + issueModel.result.errorMessage + "</div></h4>")
    else
        for key,value of issues.result.wordLevelData
            oneIssueStat = $("<div><ul>" + key + " :: " + value + "</div></ul>")
            issueStat.append(oneIssueStat)
        issueStat.append("</div>")
    $("#issue-stat-info").append(issueStat)