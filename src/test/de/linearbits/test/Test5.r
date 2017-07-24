require(sdcMicro)
data <- readMicrodata(path="C:/Users/prasser/git/suda2/data/ihsn.csv", type="csv", header=FALSE, sep=";")
su <- suda2(data)
su$score
su$disScore