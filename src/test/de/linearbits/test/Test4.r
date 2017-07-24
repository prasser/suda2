require(sdcMicro)
tab <- readMicrodata(path="C:/Users/prasser/git/suda2/data/ihsn.csv", type="csv", header=TRUE, sep=";")
su <- suda2(tab)
su$score