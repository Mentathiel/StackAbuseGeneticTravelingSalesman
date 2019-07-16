import java.util.*;

public class ÜberSalesmensch {
    private int generationSize;
    private int genomeSize;
    private int numberOfCities;
    private int reproductionSize;
    private int maxIterations;
    private float mutationRate;
    private int tournamentSize;
    private SelectionType selectionType;
    private int[][] travelPrices;
    private int startingCity;
    private int targetFitness;
    /*
    self.target = target                                     # Niska koja se pogadja
    self.gene_length = len(target)                           # Duzina niske koja se pogadja
    self.possible_gene_values = possible_gene_values         # Dozvoljene vrednosti koje mogu biti u genu
    self.num_gene_values = len(possible_gene_values)         # Duzina dozvoljenih vrednosti
    */

    public ÜberSalesmensch(int numberOfCities, SelectionType selectionType, int[][] travelPrices, int startingCity, int targetFitness){
        this.numberOfCities = numberOfCities;
        this.genomeSize = numberOfCities-1;
        this.selectionType = selectionType;
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.targetFitness = targetFitness;

        generationSize = 5000;
        reproductionSize = 200;
        maxIterations = 1000;
        mutationRate = 0.1f;
        tournamentSize = 40;
    }

    public List<SalesmanGenome> initialPopulation(){
        List<SalesmanGenome> population = new ArrayList<>();
        for(int i=0; i<generationSize; i++){
            population.add(new SalesmanGenome(numberOfCities, travelPrices, startingCity));
        }
        return population;
    }

    public List<SalesmanGenome> selection(List<SalesmanGenome> population){
        List<SalesmanGenome> selected = new ArrayList<>();
        SalesmanGenome winner;
        for(int i=0; i<reproductionSize; i++){
            if(selectionType == SelectionType.ROULETTE){
                selected.add(rouletteSelection(population));
            }
            else if(selectionType == SelectionType.TOURNAMENT){
                selected.add(tournamentSelection(population));
            }
        }

        return selected;
    }

    public SalesmanGenome rouletteSelection(List<SalesmanGenome> population){
        int totalFitness = population.stream().map(SalesmanGenome::getFitness).mapToInt(Integer::intValue).sum();
        Random random = new Random();
        int selectedValue = random.nextInt(totalFitness);
        float recValue = (float) 1/selectedValue;
        float currentSum = 0;
        for(SalesmanGenome genome : population){
            currentSum += (float) 1/genome.getFitness();
            if(currentSum>=recValue){
                return genome;
            }
        }
        int selectRandom = random.nextInt(generationSize);
        return population.get(selectRandom);
    }

    public static <E> List<E> pickNRandomElements(List<E> list, int n) {
        Random r = new Random();
        int length = list.size();

        if (length < n) return null;

        for (int i = length - 1; i >= length - n; --i)
        {
            Collections.swap(list, i , r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    public SalesmanGenome tournamentSelection(List<SalesmanGenome> population){
        List<SalesmanGenome> selected = pickNRandomElements(population,tournamentSize);
        return Collections.min(selected);
    }

    public SalesmanGenome mutate(SalesmanGenome salesman){
        Random random = new Random();
        List<Integer> genome = salesman.getGenome();
        Collections.swap(genome, random.nextInt(genomeSize), random.nextInt(genomeSize));
        return new SalesmanGenome(genome, numberOfCities, travelPrices, startingCity);
    }

    public List<SalesmanGenome> createGeneration(List<SalesmanGenome> population){
        List<SalesmanGenome> generation = new ArrayList<>();
        int currentGenerationSize = 0;
        while(currentGenerationSize < generationSize){
            List<SalesmanGenome> parents = pickNRandomElements(population,2);
            List<SalesmanGenome> children = crossover(parents);
            children.set(0, mutate(children.get(0)));
            children.set(1, mutate(children.get(1)));
            generation.addAll(children);
            currentGenerationSize+=2;
        }
        return generation;
    }

    public List<SalesmanGenome> crossover(List<SalesmanGenome> parents){
        Random random = new Random();
        int breakPoint = random.nextInt(genomeSize);
        List<SalesmanGenome> children = new ArrayList<>();

        List<Integer> parent1Genome = parents.get(0).getGenome();
        List<Integer> parent2Genome = parents.get(1).getGenome();

        for(int i = breakPoint; i<genomeSize; i++){
            int newVal;
            if(i<breakPoint){
                newVal = parent2Genome.get(i);
                Collections.swap(parent1Genome,parent1Genome.indexOf(newVal),i);
            }
            else{
                newVal = parent1Genome.get(i);
                Collections.swap(parent2Genome,parent2Genome.indexOf(newVal),i);
            }
        }

        children.add(new SalesmanGenome(parent1Genome,numberOfCities,travelPrices,startingCity));
        children.add(new SalesmanGenome(parent2Genome,numberOfCities,travelPrices,startingCity));

        return children;
    }

    public SalesmanGenome optimize(){
        List<SalesmanGenome> population = initialPopulation();
        SalesmanGenome globalBestGenome = population.get(0);
        for(int i=0; i<maxIterations; i++){
            List<SalesmanGenome> selected = selection(population);
            population = createGeneration(selected);
            globalBestGenome = Collections.min(population);
            if(globalBestGenome.getFitness() < targetFitness)
                break;
        }
        return globalBestGenome;
    }

    public void printGeneration(List<SalesmanGenome> generation ){
        for( SalesmanGenome genome : generation){
            System.out.println(genome);
        }
    }
}
