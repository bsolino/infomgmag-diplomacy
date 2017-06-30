library(plotly)

loadData <- function(){
  logs <- lapply(list.files(pattern="*Personality.log", recursive = T), read.table, sep=",", header=T)
  
  for(i in 1:length(logs)){
    data <- logs[[i]]
    names(data) <- c(names(data[1:22]), "n_prop_received", names(data[24]), "n_supply_centers")
    data$n_supply_centers <-  as.integer(gsub(";", " ", data$n_supply_centers))
    data$currentPower <- trimws(data$currentPower)
    data$personalityType <- trimws(data$personalityType)
    data$gamePhase <- trimws(data$gamePhase)
    data[6:19][sapply(data[6:19], class)=="factor"] <- NA
    logs[[i]] <- data
  }
  return(logs)
}


bindData <- function(logs){
  df <- data.frame()
  
  for(i in 1:length(logs)){
    df <- rbind(df, logs[[i]])
  }
  return(df)
}

addRoundNr <- function(data){
  data <- cbind(data[1:5], roundNr = 0, data[6:25])
  
  for(i in 1:nrow(data)){
    if(data$gamePhase[i] == data$gamePhase[1]){
      counter <- 1
    }
    else {
      counter <- counter + 1
    }
    data$roundNr[i] <- counter
  }
  return(data)
}

dfForeachTrait <- function(data, trait){
  v <- unique(trait)
  dataPerTrait <- list()
  for(i in 1:length(v)){
    dataPerTrait[[i]] <- data[trait==v[i],]
  }
  return(dataPerTrait)
} 

showCorrs <- function(ds){
  cors <- cor(ds, use="pairwise.complete.obs")
  cors[lower.tri(cors,diag=TRUE)] <- NA
  cors <- as.data.frame(as.table(cors))
  cors <- na.omit(cors)
  cors <- cors[order(-abs(cors$Freq)),]
  names(cors) <- c("variable 1", "variable 2", "correlation")
  return(cors)
}

corrsWithPvals <- function(data){
  
  combinations <- as.data.frame(t(combn(length(data), 2, simplify=TRUE)))
  
  col1 <- c(); col2 <- c(); col3 <- c(); col4 <- c()
  
  for(i in 1:nrow(combinations)){
    cor <- cor.test(as.numeric(unlist(data[combinations[i,1]])), as.numeric(unlist(data[combinations[i,2]])))
    col1[i] <- c(names(data[combinations[i,1]]))
    col2[i] <- c(names(data[combinations[i,2]]))
    col3[i] <- c(cor$estimate[[1]])
    col4[i] <- c(cor$p.value)
  }
  cors <- as.data.frame(na.omit(data.frame(variable1 = col1, 
                                           variable2 = col2, 
                                           correlation = round(col3,2), 
                                           pvalue = round(col4,2))))
  cors <- cors[order(-abs(cors$correlation)),]
  return(cors)
}

addRates <- function(data){
    data$negotiation_success_rate <- data$d_n_my_prop_accepted/data$d_n_prop_made
    data$proposal_acceptance_rate <- data$d_n_prop_accepted/data$d_n_prop_received
    data$negotiation_success_cumulative_rate <- data$n_my_prop_accepted/data$n_prop_made
    data$proposal_acceptance_cumulative_rate <- data$n_prop_accepted/data$n_prop_received
    
    data[is.na(data$negotiation_success_rate),]$negotiation_success_rate <- 0
    data[is.na(data$proposal_acceptance_rate),]$proposal_acceptance_rate <- 0
    data[is.na(data$negotiation_success_cumulative_rate),]$negotiation_success_cumulative_rate <- 0
    data[is.na(data$proposal_acceptance_cumulative_rate),]$proposal_acceptance_cumulative_rate <- 0
    
    return(data)
}

addMeans <- function(data){
  for(i in 1:nrow(data)){
    data$lmean[i] <- mean(data[i,c(7:13)][!is.na(data[i,c(7:13)])])
    data$tmean[i] <- mean(data[i,c(14:20)][!is.na(data[i,c(14:20)])])
  }
  return(data)
}

addMeansTowardsAgent <- function(data){
  for(i in 1:nrow(data)){
    gamenr <- data$gameNR[i]
    roundnr <- data$roundNr[i]
    currentPower <- data$currentPower[i]
    rows <- data[data$currentPower!= currentPower
                 & data$gameNR==gamenr
                 & data$roundNr==roundnr,]
    data$lmean_tme[i] <- mean(unlist(rows[grep(currentPower, colnames(rows))][1]))
    data$tmean_tme[i] <- mean(unlist(rows[grep(currentPower, colnames(rows))][2]))
  }
  return(data)
}

onlyLast <- function(data){
  data$diffround <- c(diff(data$roundNr),0)
  data <- data[data$diffround <= 0,]
  data <- data[-length(data)]
  return(data)
}

summaryTable <- function(summarize_by){
  
  summary <- with(dataLast[c(3,4,7:length(dataLast))], aggregate(dataLast[c(7:length(dataLast))], by=list(names = summarize_by), 
                                                                     FUN=mean, rm.na=TRUE))
  victory_rate <- c()
  solo_victory_rate <- c()
  
  for(i in 1:length(unique(summarize_by))){
    victory_rate <- with(dataLast, c(victory_rate, nrow(dataLast[summarize_by==summary$names[i] & dataLast$victory==TRUE,])/(nrow(dataLast)/7)))
    solo_victory_rate <- with(dataLast, c(solo_victory_rate, nrow(dataLast[summarize_by==summary$names[i] & dataLast$solovictory==TRUE,])/(nrow(dataLast)/7)))
  }
  
  summary$victory_rate <- victory_rate
  summary$solo_victory_rate <- solo_victory_rate
  
  return(summary)
}

addVictories <- function(data){
  data$victory <- FALSE
  data$solovictory <- FALSE
  dataagg <- onlyLast(data)
  dataagg <- data.frame(gameNR = dataagg$gameNR, n_supply_centers = dataagg$n_supply_centers)
  dataagg <- aggregate(dataagg, by=list(dataagg$gameNR), FUN=max)[2:3]
  data$diffround <- c(diff(data$roundNr),0)
  
  for(i in 1:nrow(dataagg)){
    data[data$diffround <= 0 
         & data$gameNR == dataagg$gameNR[i] 
         & data$n_supply_centers == dataagg$n_supply_centers[i],]$victory <- TRUE
    if(dataagg$n_supply_centers[i]>=18){
      data[data$diffround <= 0 
           & data$gameNR == dataagg$gameNR[i] 
           & data$n_supply_centers == dataagg$n_supply_centers[i],]$solovictory <- TRUE
    }
  }
  
  data <- data[-length(data)]
  return(data)
}

addDummies <- function(data){
  for(i in 1:length(unique(data$currentPower))){
    data[paste("pow", unique(data$currentPower)[i], sep="_")] <- (data$currentPower == unique(data$currentPower)[i])
  }
  
  for(i in 1:length(unique(data$personalityType))){
    data[paste("per", unique(data$personalityType)[i], sep="_")] <- (data$personalityType == unique(data$personalityType)[i])
  }
  return(data)
}

# gameData <- function(data){
#   d <- unique(data[2:4])
#   return(d[order(d$gameNR, d$currentPower),])
# }

plotbar <- function(data, x, y, ymax, name="", title="", xtitle="", ytitle=""){
  plot_ly(data = data, x = x,        
          y = y,
          name = name,
          type = "bar")%>%
    layout(title = title,
           xaxis = list(title = xtitle),
           yaxis = list(title = ytitle, 
                        range = c(0,ceiling(ymax*10)/10)))
}

onlyNumeric <- function(data){
  data <- data[sapply(data, class)=="logical" |
                 sapply(data, class)=="numeric" |
                 sapply(data,class)=="integer"]
}

correctGameNr <- function(data){
  data$gameNR2 <- 0
  for(i in 1:nrow(unique(data[1:2]))){
    timestamp <- unique(data[1:2])[i,1]
    gamenr <- unique(data[1:2])[i,2]
    
    data[data$timeStamp==timestamp & data$gameNR==gamenr,]$gameNR2 <- i
  }
  data$gameNR <- data$gameNR2
  data <- data[-length(data)]
}

addDeltas <- function(data){
  rows <- data[grep("n_", colnames(data))][1:6]
  rows2 <- data[grep("mean", colnames(data))]
  
  for(i in 1:length(rows)){
    data[paste("d", names(rows[i]), sep="_")] <- c(0, diff(as.matrix(rows[i])))
    
  }
  
  for(i in 1:length(rows2)){
    data[paste("d", names(rows2[i]), sep="_")] <- c(0, diff(as.matrix(rows2[i])))
  }
  
  data[data$round == 1, (length(data)-9) : length(data)] <- 0
  
  return(data)
}

savePlot <- function(plot, name){
  png(paste(name,".png", sep=""), width = 1500, height = 400)
  plot
  dev.off()
}

ttestMatrix <- function(data, var_test, var_names){
  ttest <- matrix(NA,length(data), length(data))
  
  for(i in 1:length(data)){
    for(j in 1:length(data)){
      var1 <- data[[i]][grep(var_test, colnames(data[[i]]))][1]
      var2 <- data[[j]][grep(var_test, colnames(data[[j]]))][1]
      ttest[i,j] <- t.test(var1, var2)$p.value
    }
  }
  rownames(ttest) <- unique(var_names)
  colnames(ttest) <- unique(var_names)
  ttest <- round(ttest,2)
  text <- paste("Tested by variable", var_test)
  return(ls = list(text, ttest))
}


#################################################################################
go_after_functions <- function(){}

setwd("C:/KASIA/Universiteit Utrecht/Agents and Games/Data Analysis")
#tournamentResults <- lapply(list.files(pattern="tournamentResults.log", recursive = T), read.table, sep=",", header=T)
logs <- loadData()
data <- bindData(logs)
data <- correctGameNr(data)
data <- addRoundNr(data)
data <- addMeans(data)
data <- addDeltas(data)
data <- addRates(data)
data <- addMeansTowardsAgent(data)
data <- addVictories(data)
data <- addDummies(data)

#tables
dataLast <- onlyLast(data)
dataPers <- dfForeachTrait(data, data$personalityType)
dataPows <- dfForeachTrait(data, data$currentPower)
dataGames <- dfForeachTrait(data, data$gameNR)
dataPersLast <- dfForeachTrait(dataLast, dataLast$personalityType)
dataPowsLast <- dfForeachTrait(dataLast, dataLast$currentPower)
summaryPers <- summaryTable(dataLast$personalityType)
summaryPows <- summaryTable(dataLast$currentPower)

go_after_data <- function(){}

#1. Statistical test to see whether the groups are statistically different (depending on personality)
#t.tests

t <- ttestMatrix(dataPersLast, "negotiation_success_cumulative_rate", data$personalityType)[[2]]
t
plot_ly(z = t, x=rownames(t), y=rownames(t), type = "heatmap")%>%
  layout(title = "P-values of T-tests on Number of Supply centers")

names(dataPersLast[[1]])
ttestMatrix(dataPersLast, "lmean", data$personalityType)
ttestMatrix(dataPersLast, "tmean", data$personalityType)

#2. Statistical test to see whether the groups are statistically different (depending on power)

ttestMatrix(dataPowsLast, "n_supply_centers", data$currentPower)
ttestMatrix(dataPowsLast, "lmean", data$currentPower)
ttestMatrix(dataPowsLast, "tmean", data$currentPower)


#3. Powers assignment to personality

ymax <- max(summaryPers$pow_AUS,
            summaryPers$pow_RUS,
            summaryPers$pow_ENG,
            summaryPers$pow_FRA,
            summaryPers$pow_GER,
            summaryPers$pow_ITA,
            summaryPers$pow_TUR)

p1 <- plot_ly(data = summaryPers, x = ~names, y = ~pow_RUS, type = 'bar', name = 'Russia') %>%
  add_trace(y = ~pow_AUS, name = 'Austria') %>%
  add_trace(y = ~pow_GER, name = 'Germany') %>%
  add_trace(y = ~pow_TUR, name = 'Turkey') %>%
  add_trace(y = ~pow_FRA, name = 'France') %>%
  add_trace(y = ~pow_ENG, name = 'England') %>%
  add_trace(y = ~pow_ITA, name = 'Italy') %>%
  layout(title = "Assignments of powers to personalities",
         yaxis = list(title = "% Assignments"), 
         xaxis = list(title = "",
                      range(0,ceiling(ymax*10)/10)), 
         barmode = 'group')

savePlot(p1, "powers_assignment.png")

cors <- corrsWithPvals(dataLast[45:55])
with(cors, cors[intersect(grep("pow_", cors$variable1), grep("per_", cors$variable2)),])


#4. Mean number of supply centres possessed at the end of the game, depending on personality.

ymax <- max(summaryPers$n_supply_centers) 
plotbar(summaryPers, ~names, ~n_supply_centers, ymax+1, "" ,"Mean nr supply centers of each personality")

cors <- corrsWithPvals(dataLast[c(26,52:55)])
with(cors, cors[intersect(grep("n_", cors$variable1), grep("per_", cors$variable2)),])


#5. Mean number of supply centres possessed at the end of the game, depending on power.

ymax <- max(summaryPows$n_supply_centers) 
plotbar(summaryPows, ~names, ~n_supply_centers, ymax+1, "" ,"Mean nr supply centers of each power")

cors <- corrsWithPvals(dataLast[c(26,45:51)])
with(cors, cors[intersect(grep("n_", cors$variable1), grep("pow_", cors$variable2)),])

#6. Mean like and trust value towards other agents at the end of the game, depending on personality.

ymax <- max(summaryPers$lmean, summaryPers$tmean) 
p1 <- plotbar(summaryPers, ~names, ~lmean, ymax, "Like")
p2 <- plotbar(summaryPers, ~names, ~tmean, ymax, "Trust", 
              "Mean feelings of each personality")
subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(27,28,52:55)])
with(cors, cors[intersect(grep("mean", cors$variable1), grep("per_", cors$variable2)),])

#6. Mean like and trust value of each power

ymax <- max(summaryPows$lmean, summaryPows$tmean) 
p1 <- plotbar(summaryPows, ~names, ~lmean, ymax, "Like")
p2 <- plotbar(summaryPows, ~names, ~tmean, ymax, "Trust", 
              "Mean feelings of each power")
subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(27,28,45:51)])
with(cors, cors[intersect(grep("mean", cors$variable1), grep("pow_", cors$variable2)),])


#8. Mean like and trust values of other agents towards each personality.

ymax <- max(summaryPers$lmean_tme, summaryPers$tmean_tme) 
p1 <- plotbar(summaryPers, ~names, ~lmean_tme, ymax, "Like")
p2 <- plotbar(summaryPers, ~names, ~tmean_tme, ymax, "Trust", 
              "Mean feelings towards each personality")
subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(41:42,52:55)])
with(cors, cors[intersect(grep("mean", cors$variable1), grep("per_", cors$variable2)),])

#9. Mean like and trust values of other agents towards each power.

ymax <- max(summaryPows$lmean_tme, summaryPows$tmean_tme) 
p1 <- plotbar(summaryPows, ~names, ~lmean_tme, ymax, "Like")
p2 <- plotbar(summaryPows, ~names, ~tmean_tme, ymax, "Trust", 
              "Mean feelings towards each power")
subplot(p1,p2)


cors <- corrsWithPvals(dataLast[c(41:42,45:51)])
with(cors, cors[intersect(grep("mean", cors$variable1), grep("pow_", cors$variable2)),])

# #10. Victories rate of each personality.
# 
# ymax <- max(summaryPers$victory, summaryPers$solovictory) 
# p1 <- plotbar(summaryPers, ~names, ~victory, ymax, "Victories", "Victory % given number of games played by personality")
# p2 <- plotbar(summaryPers, ~names, ~solovictory, ymax, "Solo victories", 
#               "Victory percentage given number of games played by personality")
# 
# subplot(p1,p2)

#Victories rate of each personality

ymax <- max(summaryPers$victory_rate, summaryPers$solo_victory_rate) 
p1 <- plotbar(summaryPers, ~names, ~victory_rate, ymax, "Victories", "Victory rates per each personality")
p2 <- plotbar(summaryPers, ~names, ~solo_victory_rate, ymax, "Solo victories", "Victory rates per each personality")

subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(43:44,52:55)])
with(cors, cors[intersect(grep("vic", cors$variable1), grep("per_", cors$variable2)),])


#11. Victories rate of each power.

ymax <- max(summaryPows$victory_rate, summaryPows$solo_victory_rate) 
p1 <- plotbar(summaryPows, ~names, ~victory_rate, ymax, "Victories", "Victory rates per each power")
p2 <- plotbar(summaryPows, ~names, ~solo_victory_rate, ymax, "Solo victories", 
              "Victory rates per each power")
subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(43:44,45:51)])
with(cors, cors[intersect(grep("vic", cors$variable1), grep("pow_", cors$variable2)),])

#16. Negotiation success rate (n_prop_conf/n_prop_made) and
#Proposal acceptance rate (n_prop_received, n_prop_accepted) for each personality.

ymax <- max(summaryPers$negotiation_success_cumulative_rate, summaryPers$proposal_acceptance_cumulative_rate) 
p1 <- plotbar(summaryPers, ~names, ~proposal_acceptance_cumulative_rate, ymax, "Ingoing")
p2 <- plotbar(summaryPers, ~names, ~negotiation_success_cumulative_rate, ymax, "Outgoing",
              "Proposal number related rates per each personality")
subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(39:40,52:55)])
with(cors, cors[intersect(grep("rate", cors$variable1), grep("per_", cors$variable2)),])


#17. Negotiation success rate (n_prop_conf/n_prop_made) and 
#Proposal acceptance rate (n_prop_received, n_prop_accepted) for each power.

ymax <- max(summaryPows$negotiation_success_rate, summaryPows$proposal_acceptance_rate) 
p1 <- plotbar(summaryPows, ~names, ~negotiation_success_rate, ymax, "Outgoing")
p2 <- plotbar(summaryPows, ~names, ~proposal_acceptance_rate, ymax, "Ingoing", 
        "Proposal number related rates per each power")
subplot(p1,p2)

cors <- corrsWithPvals(dataLast[c(39:40,45:51)])
with(cors, cors[intersect(grep("rate", cors$variable1), grep("pow_", cors$variable2)),])

#18. Correlations in the dataset

corsTable <- showCorrs(onlyNumeric(dataLast))
corsTable[corsTable$correlation>=0.3,]

cors <- cor(onlyNumeric(dataLast), use="pairwise.complete.obs")
plot_ly(z = round(cors,2), x=rownames(cors), y=rownames(cors), type = "heatmap")%>%
  layout(title = "Correlations between variables")