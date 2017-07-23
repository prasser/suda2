require(sdcMicro)
data <- readMicrodata(path="./data/test.csv", type="csv", header=FALSE, sep=";")
su <- suda2(data)
su$score